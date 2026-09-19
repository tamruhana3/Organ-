package com.netforge.app.domain.tunnel

import android.os.ParcelFileDescriptor
import com.netforge.app.domain.model.Metrics
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.logging.ConsoleBus
import com.netforge.app.service.TunForwarder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetSocketAddress
import java.net.Socket
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicBoolean

class DirectTunnel(
    private val profile: Profile
) : TunnelEngine {

    private val _phaseFlow = MutableStateFlow(TunnelPhase.Ready)
    override val phaseFlow: StateFlow<TunnelPhase> = _phaseFlow.asStateFlow()

    private val _metricsFlow = MutableStateFlow(Metrics())
    override val metricsFlow: StateFlow<Metrics> = _metricsFlow.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var socket: Socket? = null
    private var forwarder: TunForwarder? = null
    private val isRunning = AtomicBoolean(false)
    private val latencyHistory = ArrayDeque<Long>(10)

    override fun open(tunFd: ParcelFileDescriptor) {
        if (isRunning.getAndSet(true)) return
        _phaseFlow.value = TunnelPhase.Opening
        ConsoleBus.info("DirectTunnel", "Opening direct TCP socket to ${profile.host}:${profile.port}")

        scope.launch {
            try {
                val sock = Socket()
                socket = sock
                val startConnect = System.currentTimeMillis()
                sock.connect(InetSocketAddress(profile.host, profile.port), 10000)
                sock.tcpNoDelay = true
                sock.soTimeout = 15000
                val initialRtt = System.currentTimeMillis() - startConnect
                recordLatency(initialRtt)

                ConsoleBus.info("DirectTunnel", "TCP handshake connected in ${initialRtt}ms")

                forwarder = TunForwarder(tunFd, profile.mtu) { packet, len ->
                    try {
                        if (sock.isConnected && !sock.isClosed) {
                            sock.getOutputStream().write(packet, 0, len)
                        }
                    } catch (_: Exception) {}
                }.also { it.start() }

                // Inbound socket reader loop
                scope.launch {
                    val inBuffer = ByteArray(profile.mtu)
                    try {
                        val inStream = sock.getInputStream()
                        while (isRunning.get() && !sock.isClosed) {
                            val count = inStream.read(inBuffer)
                            if (count > 0) {
                                forwarder?.writePacket(inBuffer, count)
                            } else if (count == -1) {
                                break
                            }
                        }
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            ConsoleBus.debug("DirectTunnel", "Inbound stream closed: ${e.message}")
                        }
                    }
                }

                _phaseFlow.value = TunnelPhase.Live
                ConsoleBus.info("DirectTunnel", "Session active — traffic routing engaged")

                // Metrics loop (every 500ms)
                var lastUp = 0L
                var lastDown = 0L
                var lastTime = System.currentTimeMillis()
                val connectedAt = System.currentTimeMillis()

                while (isRunning.get()) {
                    delay(500)
                    val now = System.currentTimeMillis()
                    val dt = (now - lastTime).coerceAtLeast(1)
                    val currentUp = forwarder?.bytesUp?.get() ?: 0L
                    val currentDown = forwarder?.bytesDown?.get() ?: 0L

                    val speedUp = ((currentUp - lastUp) * 1000L) / dt
                    val speedDown = ((currentDown - lastDown) * 1000L) / dt
                    lastUp = currentUp
                    lastDown = currentDown
                    lastTime = now

                    val jitter = computeJitter()
                    val latestPing = latencyHistory.lastOrNull() ?: 24L

                    _metricsFlow.value = Metrics(
                        bytesUp = currentUp,
                        bytesDown = currentDown,
                        pingMs = latestPing,
                        jitterMs = jitter,
                        speedUpBps = speedUp,
                        speedDownBps = speedDown,
                        connectedAt = connectedAt
                    )
                }

            } catch (e: Exception) {
                ConsoleBus.error("DirectTunnel", "Connection failed: ${e.message}", e.stackTraceToString())
                _phaseFlow.value = TunnelPhase.Error
                close()
            }
        }
    }

    private fun recordLatency(ms: Long) {
        if (latencyHistory.size >= 10) latencyHistory.pollFirst()
        latencyHistory.addLast(ms)
    }

    private fun computeJitter(): Double {
        if (latencyHistory.size < 2) return 0.0
        val mean = latencyHistory.average()
        val variance = latencyHistory.map { Math.pow(it - mean, 2.0) }.average()
        return Math.sqrt(variance)
    }

    override fun close() {
        if (!isRunning.getAndSet(false)) return
        ConsoleBus.info("DirectTunnel", "Closing direct tunnel session")
        try { forwarder?.stop() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        scope.cancel()
        if (_phaseFlow.value != TunnelPhase.Error) {
            _phaseFlow.value = TunnelPhase.Halted
        }
    }
}
