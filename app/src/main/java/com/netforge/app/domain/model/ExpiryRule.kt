package com.netforge.app.domain.model

enum class ExpiryMode {
    Block, Warn
}

enum class ExpiryType {
    Duration, AbsoluteDate
}

data class ExpiryRule(
    val enabled: Boolean = false,
    val type: ExpiryType = ExpiryType.Duration,
    val epochMs: Long = 0L,
    val durationMs: Long = 86400000L, // 24h default
    val warningText: String = "This profile has expired.",
    val mode: ExpiryMode = ExpiryMode.Block
) {
    fun isExpired(): Boolean {
        if (!enabled) return false
        val expireTime = if (type == ExpiryType.AbsoluteDate) epochMs else epochMs + durationMs
        return System.currentTimeMillis() > expireTime
    }
}
