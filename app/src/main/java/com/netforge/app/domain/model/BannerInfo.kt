package com.netforge.app.domain.model

enum class BannerStyle {
    Info, Warning, Success, Custom
}

data class BannerInfo(
    val enabled: Boolean = false,
    val style: BannerStyle = BannerStyle.Info,
    val message: String = "",
    val customColorHex: String = "#7C5CFF",
    val durationSeconds: Int = 5, // 0 = until dismissed
    val buttonLabel: String = "",
    val buttonUrl: String = ""
)
