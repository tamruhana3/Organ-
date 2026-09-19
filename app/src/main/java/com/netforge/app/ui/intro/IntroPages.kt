package com.netforge.app.ui.intro

data class IntroPageData(
    val title: String,
    val subtitle: String,
    val badge: String,
    val iconEmoji: String
)

val introPagesList = listOf(
    IntroPageData(
        badge = "CLEAN-ROOM ARCHITECTURE",
        title = "Private routing,\nplainly done.",
        subtitle = "NetForge routes your network traffic through hardened, encrypted tunnels with zero tracking, telemetry, or third-party wrappers.",
        iconEmoji = "🛡️"
    ),
    IntroPageData(
        badge = "VERSATILE TRANSPORTS",
        title = "Multi-Protocol\nEncapsulation.",
        subtitle = "Seamlessly switch between Direct TCP, Wrapped TLS, Custom Payload Injection, DNS Slow Channels, and WebSocket Live streams.",
        iconEmoji = "⚡"
    ),
    IntroPageData(
        badge = "SOVEREIGN CONFIGURATIONS",
        title = "Sealed .nfg Vaults.",
        subtitle = "Export and import profiles protected with memory-hard Argon2id KDF, AES-256-GCM encryption, hardware device binding, and tamper guards.",
        iconEmoji = "🔒"
    )
)
