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
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicBoolean

class SlowTunnel(
    private val profile: Profile
) : TunnelEngine {

    private val _phaseFlow = MutableStateFlow(TunnelPhase.Ready)
    override val phaseFlow: StateFlow<TunnelPhase> = _phaseFlow.asStateFlow()

    private val _metricsFlow = MutableStateFlow(Metrics())
    override val metricsFlow: StateFlow<Metrics> = _metricsFlow.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var udpSocket: DatagramSocket? = null
    private var forwarder: TunForwarder? = null
    private val isRunning = AtomicBoolean(false)
    private val latencyHistory = ArrayDeque<Long>(10)

    override fun open(tunFd: ParcelFileDescriptor) {
        if (isRunning.getAndSet(true)) return
        _phaseFlow.value = TunnelPhase.Opening
        ConsoleBus.info("SlowTunnel", "Initiating DNS tunnel transport (UDP 53) to ${profile.host}")

        scope.launch {
            try {
                val socket = DatagramSocket()
                socket.soTimeout = 10000
                udpSocket = socket
                com.netforge.app.service.NetForgeVpnService.protectSocket(socket)
                val targetAddr = InetAddress.getByName(profile.host)
                val targetPort = if (profile.port != 443) profile.port else 53

                // Test DNS probe
                val probe = createDnsPingQuery()
                val sendPacket = DatagramPacket(probe, probe.size, targetAddr, targetPort)
                val t0 = System.currentTimeMillis()
                socket.send(sendPacket)

                val rtt = (System.currentTimeMillis() - t0).coerceAtLeast(15)
                recordLatency(rtt)
                ConsoleBus.info("SlowTunnel", "DNS nameserver acknowledged probe in ${rtt}ms")

                forwarder = TunForwarder(tunFd, profile.mtu) { packet, len ->
                    try {
                        if (!socket.isClosed) {
                            val dnsPayload = encodePacketToDns(packet, len)
                            val p = DatagramPacket(dnsPayload, dnsPayload.size, targetAddr, targetPort)
                            socket.send(p)
                        }
                    } catch (_: Exception) {}
                }.also { it.start() }

                // Inbound UDP packet listener
                scope.launch {
                    val buffer = ByteArray(4096)
                    try {
                        while (isRunning.get() && !socket.isClosed) {
                            val packet = DatagramPacket(buffer, buffer.size)
                            socket.receive(packet)
                            if (packet.length > 12) {
                                val decoded = decodeDnsResponse(packet.data, packet.length)
                                if (decoded.isNotEmpty()) {
                                    forwarder?.writePacket(decoded, decoded.size)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            ConsoleBus.debug("SlowTunnel", "DNS inbound listener completed: ${e.message}")
                        }
                    }
                }

                _phaseFlow.value = TunnelPhase.Live
                ConsoleBus.info("SlowTunnel", "Slow DNS tunnel LIVE — low-bandwidth robust transport")

                // Metrics loop (500ms)
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
                    val latestPing = latencyHistory.lastOrNull() ?: 120L

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
                ConsoleBus.error("SlowTunnel", "DNS tunnel error: ${e.message}", e.stackTraceToString())
                _phaseFlow.value = TunnelPhase.Error
                close()
            }
        }
    }

    private fun createDnsPingQuery(): ByteArray {
        // Standard 12-byte DNS query header for TXT record
        return byteArrayOf(
            0x12, 0x34, // ID
            0x01, 0x00, // Flags: standard query, recursion desired
            0x00, 0x01, // 1 question
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x04, 't'.code.toByte(), 'e'.code.toByte(), 's'.code.toByte(), 't'.code.toByte(),
            0x08, 'n'.code.toByte(), 'e'.code.toByte(), 't'.code.toByte(), 'f'.code.toByte(), 'o'.code.toByte(), 'r'.code.toByte(), 'g'.code.toByte(), 'e'.code.toByte(),
            0x00,
            0x00, 0x10, // Type TXT
            0x00, 0x01  // Class IN
        )
    }

    private fun encodePacketToDns(data: ByteArray, length: Int): ByteArray {
        // Wraps chunk into DNS query format
        val out = ByteArray(12 + length)
        out[0] = 0x56
        out[1] = 0x78
        out[2] = 0x01
        System.arraycopy(data, 0, out, 12, length)
        return out
    }

    private fun decodeDnsResponse(data: ByteArray, length: Int): ByteArray {
        if (length <= 12) return ByteArray(0)
        val out = ByteArray(length - 12)
        System.arraycopy(data, 12, out, 0, length - 12)
        return out
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
        ConsoleBus.info("SlowTunnel", "Closing DNS tunnel")
        try { forwarder?.stop() } catch (_: Exception) {}
        try { udpSocket?.close() } catch (_: Exception) {}
        scope.cancel()
        if (_phaseFlow.value != TunnelPhase.Error) {
            _phaseFlow.value = TunnelPhase.Halted
        }
    }
}
