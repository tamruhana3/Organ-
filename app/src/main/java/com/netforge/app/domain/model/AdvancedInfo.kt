package com.netforge.app.domain.model

data class AdvancedInfo(
    val signatureTag: String = "",
    val watermarkId: String = "",
    val tamperProtection: Boolean = true
)
