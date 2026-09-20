package com.netforge.app.domain.model

enum class Mode(val displayName: String, val description: String) {
    CustomPayload("Custom Payload", "HTTP header injection with custom bug host payload"),
    SslTunnel("SSL Tunnel", "Direct TLS / SSL tunnel with SNI spoofing"),
    SslProxy("SSL + Proxy", "SSL tunnel forwarded through HTTP/HTTPS proxy"),
    SslHttp("SSL + HTTP", "SSL handshake encapsulated with custom HTTP headers"),
    SlowDns("Slow DNS", "DNS tunnel over UDP port 53 (TXT / CNAME queries)"),
    SshDirect("SSH Direct", "Direct SSH TCP handshake with clean connection"),

    // Backward-compatible variants
    Direct("Direct", "Raw TCP socket to host:port"),
    Wrapped("SSL / TLS", "TLS socket to host:port with SNI"),
    WrappedPlus("Wrapped+", "TLS socket with custom payload template headers"),
    Slow("Slow (DNS)", "UDP 53 DNS tunnel transport"),
    Live("WebSocket / Live", "TCP + HTTP Upgrade, WebSocket frames")
}
