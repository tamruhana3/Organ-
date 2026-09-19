package com.netforge.app.domain.model

enum class LimitMode {
    Block, Warn
}

enum class LimitReset {
    Never, Daily, Weekly
}

data class LimitRule(
    val enabled: Boolean = false,
    val maxImports: Int = 3,
    val resetOn: LimitReset = LimitReset.Never,
    val mode: LimitMode = LimitMode.Block
)
