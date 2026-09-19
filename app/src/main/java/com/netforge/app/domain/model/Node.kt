package com.netforge.app.domain.model

data class Node(
    val id: String,
    val name: String,
    val countryCode: String,
    val host: String,
    val port: Int = 443,
    val flagEmoji: String,
    val latencyMs: Int = 45,
    val isOnline: Boolean = true,
    val publicKey: String? = null
)
