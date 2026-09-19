package com.netforge.app.service

import android.os.ParcelFileDescriptor
import com.netforge.app.domain.model.Metrics
import com.netforge.app.logging.ConsoleBus
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class TunForwarder(
    private val tunFd: ParcelFileDescriptor,
    private val mtu: Int = 1500,
    private val onPacketOut: ((ByteArray, Int) -> Unit)? = null
) {
    private val running = AtomicBoolean(false)
    val bytesUp = AtomicLong(0L)
    val bytesDown = AtomicLong(0L)

    private var readThread: Thread? = null
    private var inStream: FileInputStream? = null
    private var outStream: FileOutputStream? = null

    fun start() {
        if (running.getAndSet(true)) return
        inStream = FileInputStream(tunFd.fileDescriptor)
        outStream = FileOutputStream(tunFd.fileDescriptor)

        readThread = Thread({
            val buffer = ByteArray(mtu)
            ConsoleBus.info("TunForwarder", "TUN interface reader loop active, MTU=$mtu")
            try {
                while (running.get()) {
                    val bytesRead = inStream?.read(buffer) ?: -1
                    if (bytesRead > 0) {
                        bytesUp.addAndGet(bytesRead.toLong())
                        onPacketOut?.invoke(buffer, bytesRead)
                    } else if (bytesRead == -1) {
                        break
                    }
                }
            } catch (e: Exception) {
                if (running.get()) {
                    ConsoleBus.debug("TunForwarder", "TUN read loop finished: ${e.message}")
                }
            }
        }, "NetForge-TunReadThread").apply { start() }
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
        readThread?.interrupt()
        readThread = null
    }
}
