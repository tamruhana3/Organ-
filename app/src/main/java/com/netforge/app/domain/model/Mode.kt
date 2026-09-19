package com.netforge.app.domain.model

enum class Mode(val displayName: String, val description: String) {
    Direct("Direct", "Raw TCP socket to host:port, clean TCP transport"),
    Wrapped("Wrapped", "TLS socket to host:port with SSH encapsulated inside TLS"),
    WrappedPlus("Wrapped+", "TLS socket with custom payload template headers injected"),
    Slow("Slow", "UDP 53 DNS tunnel (iodine / dnstt payload transport)"),
    Live("Live", "TCP + HTTP Upgrade, WebSocket frames with SSH inside")
}
