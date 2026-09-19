package com.netforge.app.domain.node

import com.netforge.app.domain.model.Node

object NodeCatalog {

    val defaultNodes = listOf(
        Node(
            id = "node-us-east",
            name = "US East (Ashburn)",
            countryCode = "US",
            host = "us-east.netforge.internal",
            port = 443,
            flagEmoji = "🇺🇸",
            latencyMs = 28,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIKn730eP8vG2xZk81t23c+kldL"
        ),
        Node(
            id = "node-de-fra",
            name = "EU Central (Frankfurt)",
            countryCode = "DE",
            host = "fra.netforge.internal",
            port = 443,
            flagEmoji = "🇩🇪",
            latencyMs = 35,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIE7bV84Zkx1P49cR392xLtK9a"
        ),
        Node(
            id = "node-uk-lon",
            name = "UK South (London)",
            countryCode = "GB",
            host = "lon.netforge.internal",
            port = 443,
            flagEmoji = "🇬🇧",
            latencyMs = 42,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIH3xY8aKl8vN293kXkLt91A2"
        ),
        Node(
            id = "node-sg-sin",
            name = "Asia South (Singapore)",
            countryCode = "SG",
            host = "sin.netforge.internal",
            port = 443,
            flagEmoji = "🇸🇬",
            latencyMs = 76,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIKxV903nLw8z2NkXmL32890Q"
        ),
        Node(
            id = "node-jp-tyo",
            name = "Asia East (Tokyo)",
            countryCode = "JP",
            host = "tyo.netforge.internal",
            port = 443,
            flagEmoji = "🇯🇵",
            latencyMs = 84,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIN99aV3kLmNpQr23xZ99P8k1"
        ),
        Node(
            id = "node-ch-zrh",
            name = "Swiss Vault (Zurich)",
            countryCode = "CH",
            host = "zrh.netforge.internal",
            port = 443,
            flagEmoji = "🇨🇭",
            latencyMs = 38,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIJ87kLmXyZ999aP8k1N99aV3"
        )
    )
}
