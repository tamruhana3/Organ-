package com.netforge.app.domain.model

data class LockInfo(
    val sealedWithPassphrase: Boolean = true,
    val passphraseHint: String = "",
    val requireTwelveChars: Boolean = false,
    val requireMixedAndSymbols: Boolean = false
)
