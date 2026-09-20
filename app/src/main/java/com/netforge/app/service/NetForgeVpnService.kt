package com.netforge.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.netforge.app.domain.model.Metrics
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.domain.tunnel.TunnelEngine
import com.netforge.app.domain.tunnel.TunnelEngineFactory
import com.netforge.app.logging.ConsoleBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

class NetForgeVpnService : VpnService() {

    companion object {
        const val ACTION_START = "com.netforge.app.action.START"
        const val ACTION_STOP = "com.netforge.app.action.STOP"
        const val EXTRA_PROFILE = "extra_profile"

        private val _phaseFlow = MutableStateFlow(TunnelPhase.Ready)
        val phaseFlow: StateFlow<TunnelPhase> = _phaseFlow.asStateFlow()

        private val _metricsFlow = MutableStateFlow(Metrics())
        val metricsFlow: StateFlow<Metrics> = _metricsFlow.asStateFlow()

        private val _activeProfileFlow = MutableStateFlow<Profile?>(null)
        val activeProfileFlow: StateFlow<Profile?> = _activeProfileFlow.asStateFlow()

        var activeInstance: NetForgeVpnService? = null
            private set

        fun protectSocket(socket: java.net.Socket?): Boolean {
            if (socket == null) return false
            return try {
                activeInstance?.protect(socket) ?: false
            } catch (e: Exception) {
                ConsoleBus.debug("NetForgeVpnService", "protect(Socket) error: ${e.message}")
                false
            }
        }

        fun protectSocket(socket: java.net.DatagramSocket?): Boolean {
            if (socket == null) return false
            return try {
                activeInstance?.protect(socket) ?: false
            } catch (e: Exception) {
                ConsoleBus.debug("NetForgeVpnService", "protect(DatagramSocket) error: ${e.message}")
                false
            }
        }

        fun start(context: Context, profile: Profile) {
            val intent = Intent(context, NetForgeVpnService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PROFILE, profile)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NetForgeVpnService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var tunInterface: ParcelFileDescriptor? = null
    private var engine: TunnelEngine? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isStopping = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
        ConsoleBus.info("NetForgeVpnService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            ConsoleBus.info("NetForgeVpnService", "Stop request received")
            tearDownSession()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_START) {
            @Suppress("DEPRECATION")
            val profile = intent.getSerializableExtra(EXTRA_PROFILE) as? Profile
            if (profile == null) {
                ConsoleBus.error("NetForgeVpnService", "No profile supplied to start intent")
                stopSelf()
                return START_NOT_STICKY
            }
            isStopping.set(false)
            startSession(profile)
        }

        return START_STICKY
    }

    private fun startSession(profile: Profile) {
        _activeProfileFlow.value = profile
        _phaseFlow.value = TunnelPhase.Opening
        ConsoleBus.info("NetForgeVpnService", "Configuring TUN interface (MTU=${profile.mtu})")

        val initialNotif = TunnelNotification.build(this, profile, TunnelPhase.Opening, Metrics())
        startForeground(TunnelNotification.NOTIFICATION_ID, initialNotif)

        serviceScope.launch {
            try {
                val builder = Builder()
                    .setSession("NetForge")
                    .addAddress("10.8.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .setMtu(profile.mtu)

                val dns1 = profile.dnsPrimary.ifBlank { "1.1.1.1" }
                builder.addDnsServer(dns1)
                if (profile.dnsSecondary.isNotBlank()) {
                    builder.addDnsServer(profile.dnsSecondary)
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    builder.setMetered(false)
                }

                val pfd = builder.establish()
                if (pfd == null) {
                    throw IllegalStateException("VPN Builder returned null descriptor (permissions revoked or VPN revoked by OS)")
                }
                tunInterface = pfd
                ConsoleBus.info("NetForgeVpnService", "TUN interface established (FD=${pfd.fd})")

                val newEngine = TunnelEngineFactory.create(profile)
                engine = newEngine

                // Monitor engine phase
                serviceScope.launch {
                    newEngine.phaseFlow.collect { phase ->
                        _phaseFlow.value = phase
                        if (phase == TunnelPhase.Error || phase == TunnelPhase.Halted) {
                            if (!isStopping.get()) {
                                tearDownSession()
                                stopSelf()
                            }
                        }
                    }
                }

                // Monitor engine metrics and update notification
                serviceScope.launch {
                    var lastNotifUpdate = 0L
                    newEngine.metricsFlow.collect { metrics ->
                        _metricsFlow.value = metrics
                        val now = System.currentTimeMillis()
                        if (now - lastNotifUpdate > 1000) {
                            lastNotifUpdate = now
                            val updatedNotif = TunnelNotification.build(
                                this@NetForgeVpnService,
                                profile,
                                _phaseFlow.value,
                                metrics
                            )
                            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            notifManager.notify(TunnelNotification.NOTIFICATION_ID, updatedNotif)
                        }
                    }
                }

                newEngine.open(pfd)

            } catch (e: Exception) {
                ConsoleBus.error("NetForgeVpnService", "TUN startup exception: ${e.message}", e.stackTraceToString())
                _phaseFlow.value = TunnelPhase.Error
                tearDownSession()
                stopSelf()
            }
        }
    }

    private fun tearDownSession() {
        if (isStopping.getAndSet(true)) return
        ConsoleBus.info("NetForgeVpnService", "Tearing down VPN session")
        try { engine?.close() } catch (_: Exception) {}
        engine = null

        try { tunInterface?.close() } catch (_: Exception) {}
        tunInterface = null

        _phaseFlow.value = TunnelPhase.Halted
        _metricsFlow.value = Metrics()
        _activeProfileFlow.value = null

        stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        tearDownSession()
        serviceScope.cancel()
        if (activeInstance == this) {
            activeInstance = null
        }
        ConsoleBus.info("NetForgeVpnService", "Service destroyed")
        _phaseFlow.value = TunnelPhase.Ready
        super.onDestroy()
    }
}
