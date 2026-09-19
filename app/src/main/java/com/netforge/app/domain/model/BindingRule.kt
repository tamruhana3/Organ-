package com.netforge.app.domain.model

data class BindingRule(
    val bindDevice: Boolean = false,
    val boundDeviceId: String = "",
    val blockRoot: Boolean = false,
    val blockEmulator: Boolean = false,
    val requireAppMatch: Boolean = false,
    val expectedPackage: String = "com.aistudio.netforge.cxtrqu"
)
