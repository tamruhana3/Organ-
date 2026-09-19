package com.netforge.app.domain.payload

data class PayloadContext(
    val host: String,
    val port: Int,
    val frontHost: String? = null,
    val sshUser: String? = null,
    val sshPass: String? = null,
    val tls: Boolean = true
)
