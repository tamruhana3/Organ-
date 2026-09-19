package com.netforge.app.domain.model

data class Payload(
    val template: String = "CONNECT [host_port] HTTP/1.1[crlf]Host: [host][crlf]User-Agent: [ua][crlf]Connection: Upgrade[crlf]Upgrade: websocket[crlf][crlf]",
    val frontHost: String = "",
    val sni: String = "",
    val customProxy: String = "",
    val customProxyPort: Int = 8080
)
