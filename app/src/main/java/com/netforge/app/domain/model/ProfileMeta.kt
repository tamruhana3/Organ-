package com.netforge.app.domain.model

data class ProfileMeta(
    val name: String = "Default Profile",
    val author: String = "NetForge user",
    val note: String = "",
    val createdAtIso: String = "",
    val version: Int = 1
)
