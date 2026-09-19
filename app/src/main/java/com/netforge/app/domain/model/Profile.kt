package com.netforge.app.domain.model

import java.io.Serializable

data class Profile(
    val id: Long = 0L,
    val name: String = "Default Profile",
    val author: String = "NetForge user",
    val note: String = "",
    val host: String = "127.0.0.1",
    val port: Int = 443,
    val mode: Mode = Mode.Wrapped,
    val sshUser: String = "netforge",
    val sshPass: String = "",
    val sshKey: String = "",
    val sni: String = "",
    val frontHost: String = "",
    val payloadTemplate: String = "CONNECT [host_port] HTTP/1.1[crlf]Host: [host][crlf]User-Agent: [ua][crlf]Connection: Upgrade[crlf]Upgrade: websocket[crlf][crlf]",
    val proxyHost: String = "",
    val proxyPort: Int = 3128,
    val dnsPrimary: String = "1.1.1.1",
    val dnsSecondary: String = "1.0.0.1",
    val customDns: String = "",
    val mtu: Int = 1500,
    val keepalive: Int = 15,
    val enableUdp: Boolean = true,
    val isFavorite: Boolean = false,
    val isEncrypted: Boolean = false,
    val expiryRule: ExpiryRule = ExpiryRule(),
    val limitRule: LimitRule = LimitRule(),
    val bindingRule: BindingRule = BindingRule(),
    val lockInfo: LockInfo = LockInfo(),
    val bannerInfo: BannerInfo = BannerInfo(),
    val advancedInfo: AdvancedInfo = AdvancedInfo(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val importCount: Int = 0
) : Serializable
