package com.netforge.app.domain.node

import com.netforge.app.domain.model.Node

object NodeCatalog {

    val defaultNodes = listOf(
        Node(
            id = "node-us-east",
            name = "USA - Ashburn / New York",
            countryCode = "US",
            host = "104.16.132.229",
            port = 443,
            flagEmoji = "🇺🇸",
            latencyMs = 24,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIKn730eP8vG2xZk81t23c+kldL"
        ),
        Node(
            id = "node-us-west",
            name = "USA - Los Angeles (Streaming)",
            countryCode = "US",
            host = "172.67.182.203",
            port = 443,
            flagEmoji = "🇺🇸",
            latencyMs = 38,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIKr9348Zk23c+kldLP8vG2xZk81t"
        ),
        Node(
            id = "node-nl-ams",
            name = "Netherlands - Amsterdam 01 (High Speed)",
            countryCode = "NL",
            host = "104.18.25.1",
            port = 443,
            flagEmoji = "🇳🇱",
            latencyMs = 29,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIH11aM8vG2xZk81t23c+kldL09A2"
        ),
        Node(
            id = "node-nl-ams2",
            name = "Netherlands - Amsterdam 02 (P2P / Gaming)",
            countryCode = "NL",
            host = "185.199.108.153",
            port = 443,
            flagEmoji = "🇳🇱",
            latencyMs = 32,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIN22bV84Zkx1P49cR392xLtK9a"
        ),
        Node(
            id = "node-de-fra",
            name = "Germany - Frankfurt (Low Latency)",
            countryCode = "DE",
            host = "1.1.1.1",
            port = 443,
            flagEmoji = "🇩🇪",
            latencyMs = 26,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIE7bV84Zkx1P49cR392xLtK9a"
        ),
        Node(
            id = "node-uk-lon",
            name = "United Kingdom - London",
            countryCode = "GB",
            host = "151.101.0.133",
            port = 443,
            flagEmoji = "🇬🇧",
            latencyMs = 34,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIH3xY8aKl8vN293kXkLt91A2"
        ),
        Node(
            id = "node-sg-sin",
            name = "Singapore - Asia Pacific",
            countryCode = "SG",
            host = "104.17.209.9",
            port = 443,
            flagEmoji = "🇸🇬",
            latencyMs = 68,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIKxV903nLw8z2NkXmL32890Q"
        ),
        Node(
            id = "node-ca-tor",
            name = "Canada - Toronto / Montreal",
            countryCode = "CA",
            host = "198.50.150.10",
            port = 443,
            flagEmoji = "🇨🇦",
            latencyMs = 41,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIJ87kLmXyZ999aP8k1N99aV3"
        ),
        Node(
            id = "node-fr-par",
            name = "France - Paris",
            countryCode = "FR",
            host = "51.15.241.1",
            port = 443,
            flagEmoji = "🇫🇷",
            latencyMs = 36,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIK11xZ99P8k1N99aV3kLmNpQ"
        ),
        Node(
            id = "node-jp-tyo",
            name = "Japan - Tokyo (Asia Fast)",
            countryCode = "JP",
            host = "133.242.18.243",
            port = 443,
            flagEmoji = "🇯🇵",
            latencyMs = 79,
            isOnline = true,
            publicKey = "ed25519:MC4CAQAwBQYDK2VwBCIEIN99aV3kLmNpQr23xZ99P8k1"
        )
    )
}
