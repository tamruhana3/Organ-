package com.netforge.app.service

import android.os.ParcelFileDescriptor
import com.netforge.app.logging.ConsoleBus
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class TunForwarder(
    private val tunFd: ParcelFileDescriptor,
    private val mtu: Int = 1500,
    private val dnsServerIp: String = "1.1.1.1",
    private val onPacketOut: ((ByteArray, Int) -> Unit)? = null
) {
    private val running = AtomicBoolean(false)
    val bytesUp = AtomicLong(0L)
    val bytesDown = AtomicLong(0L)

    private var readThread: Thread? = null
    private var inStream: FileInputStream? = null
    private var outStream: FileOutputStream? = null
    private val dnsExecutor = Executors.newFixedThreadPool(4)
    private var dnsSocket: DatagramSocket? = null

    fun start() {
        if (running.getAndSet(true)) return
        inStream = FileInputStream(tunFd.fileDescriptor)
        outStream = FileOutputStream(tunFd.fileDescriptor)

        try {
            val sock = DatagramSocket()
            NetForgeVpnService.protectSocket(sock)
            dnsSocket = sock
        } catch (e: Exception) {
            ConsoleBus.debug("TunForwarder", "DNS socket init: ${e.message}")
        }

        readThread = Thread({
            val buffer = ByteArray(mtu)
            ConsoleBus.info("TunForwarder", "TUN interface reader loop active, MTU=$mtu")
            try {
                while (running.get()) {
                    val bytesRead = inStream?.read(buffer) ?: -1
                    if (bytesRead > 0) {
                        bytesUp.addAndGet(bytesRead.toLong())

                        // Check if packet is IPv4
                        if (bytesRead >= 20 && (buffer[0].toInt() and 0xF0) == 0x40) {
                            val protocol = buffer[9].toInt() and 0xFF

                            if (protocol == 17 && bytesRead >= 28) { // UDP
                                val destPort = ((buffer[22].toInt() and 0xFF) shl 8) or (buffer[23].toInt() and 0xFF)
                                if (destPort == 53) {
                                    // Handle DNS query
                                    val packetCopy = buffer.copyOf(bytesRead)
                                    dnsExecutor.submit {
                                        handleDnsQuery(packetCopy, bytesRead)
                                    }
                                    continue
                                }
                            } else if (protocol == 1 && bytesRead >= 28) { // ICMP
                                val icmpType = buffer[20].toInt() and 0xFF
                                if (icmpType == 8) { // Echo Request
                                    val icmpReply = handleIcmpEcho(buffer, bytesRead)
                                    if (icmpReply != null) {
                                        writePacket(icmpReply, icmpReply.size)
                                        continue
                                    }
                                }
                            }
                        }

                        // Forward other packets through tunnel engine
                        try {
                            onPacketOut?.invoke(buffer, bytesRead)
                        } catch (_: Exception) {}
                    } else if (bytesRead == -1) {
                        break
                    }
                }
            } catch (e: Exception) {
                if (running.get()) {
                    ConsoleBus.debug("TunForwarder", "TUN read loop finished: ${e.message}")
                }
            }
        }, "FlexNet-TunReadThread").apply { start() }
    }

    private fun handleDnsQuery(packet: ByteArray, length: Int) {
        val sock = dnsSocket ?: return
        try {
            val udpLength = ((packet[24].toInt() and 0xFF) shl 8) or (packet[25].toInt() and 0xFF)
            val dnsPayloadLen = udpLength - 8
            if (dnsPayloadLen <= 0 || 28 + dnsPayloadLen > length) return

            val dnsQuery = ByteArray(dnsPayloadLen)
            System.arraycopy(packet, 28, dnsQuery, 0, dnsPayloadLen)

            val dnsServer = InetAddress.getByName(dnsServerIp)
            val sendPacket = DatagramPacket(dnsQuery, dnsPayloadLen, dnsServer, 53)
            sock.send(sendPacket)

            val recvBuffer = ByteArray(1500)
            val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
            sock.soTimeout = 4000
            sock.receive(recvPacket)

            val dnsRespLen = recvPacket.length
            val totalRespLen = 20 + 8 + dnsRespLen
            val respPacket = ByteArray(totalRespLen)

            // IPv4 header
            respPacket[0] = 0x45
            respPacket[1] = 0x00
            respPacket[2] = ((totalRespLen shr 8) and 0xFF).toByte()
            respPacket[3] = (totalRespLen and 0xFF).toByte()
            respPacket[4] = packet[4]
            respPacket[5] = packet[5]
            respPacket[6] = 0x40 // Don't fragment
            respPacket[7] = 0x00
            respPacket[8] = 64 // TTL
            respPacket[9] = 17 // UDP
            // Checksum computed later

            // Source IP is original Dest IP
            System.arraycopy(packet, 16, respPacket, 12, 4)
            // Dest IP is original Source IP
            System.arraycopy(packet, 12, respPacket, 16, 4)

            // Calculate IPv4 Header Checksum
            val ipChecksum = computeIpChecksum(respPacket, 0, 20)
            respPacket[10] = ((ipChecksum shr 8) and 0xFF).toByte()
            respPacket[11] = (ipChecksum and 0xFF).toByte()

            // UDP Header
            // Source Port = original Dest Port (53)
            respPacket[20] = packet[22]
            respPacket[21] = packet[23]
            // Dest Port = original Source Port
            respPacket[22] = packet[20]
            respPacket[23] = packet[21]
            val udpTotalLen = 8 + dnsRespLen
            respPacket[24] = ((udpTotalLen shr 8) and 0xFF).toByte()
            respPacket[25] = (udpTotalLen and 0xFF).toByte()
            respPacket[26] = 0 // Checksum optional in IPv4 UDP
            respPacket[27] = 0

            // UDP Data
            System.arraycopy(recvBuffer, 0, respPacket, 28, dnsRespLen)

            writePacket(respPacket, totalRespLen)
        } catch (_: Exception) {
            // DNS resolution timeout or error
        }
    }

    private fun handleIcmpEcho(packet: ByteArray, length: Int): ByteArray? {
        val reply = packet.copyOf(length)
        // Swap IP addresses
        for (i in 0..3) {
            val tmp = reply[12 + i]
            reply[12 + i] = reply[16 + i]
            reply[16 + i] = tmp
        }
        // Set ICMP type to 0 (Echo Reply)
        reply[20] = 0x00
        // Recalculate ICMP checksum
        reply[22] = 0
        reply[23] = 0
        val icmpChecksum = computeIpChecksum(reply, 20, length - 20)
        reply[22] = ((icmpChecksum shr 8) and 0xFF).toByte()
        reply[23] = (icmpChecksum and 0xFF).toByte()

        // Recalculate IP checksum
        reply[10] = 0
        reply[11] = 0
        val ipChecksum = computeIpChecksum(reply, 0, 20)
        reply[10] = ((ipChecksum shr 8) and 0xFF).toByte()
        reply[11] = (ipChecksum and 0xFF).toByte()
        return reply
    }

    private fun computeIpChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var i = offset
        while (i < offset + length - 1) {
            val word = ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        if (i < offset + length) {
            sum += (data[i].toInt() and 0xFF) shl 8
        }
        while (sum shr 16 > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return (sum.inv()) and 0xFFFF
    }

    fun writePacket(data: ByteArray, length: Int) {
        if (!running.get()) return
        try {
            outStream?.write(data, 0, length)
            bytesDown.addAndGet(length.toLong())
        } catch (_: Exception) {
            // Buffer/socket closing
        }
    }

    fun stop() {
        running.set(false)
        try { inStream?.close() } catch (_: Exception) {}
        try { outStream?.close() } catch (_: Exception) {}
        try { dnsSocket?.close() } catch (_: Exception) {}
        dnsExecutor.shutdownNow()
        readThread?.interrupt()
        readThread = null
    }
}
