package com.netforge.app.domain.model

data class Metrics(
    val bytesUp: Long = 0L,
    val bytesDown: Long = 0L,
    val pingMs: Long = 0L,
    val jitterMs: Double = 0.0,
    val speedUpBps: Long = 0L,
    val speedDownBps: Long = 0L,
    val connectedAt: Long = 0L
) {
    fun formattedUp(): String = formatBytes(bytesUp)
    fun formattedDown(): String = formatBytes(bytesDown)
    fun formattedSpeedUp(): String = "${formatBytes(speedUpBps)}/s"
    fun formattedSpeedDown(): String = "${formatBytes(speedDownBps)}/s"

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
            val unit = "KMGTPE"[exp - 1]
            return String.format(java.util.Locale.US, "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), unit)
        }
    }
}
