package com.netforge.app.domain.payload

import java.security.SecureRandom

object PayloadEngine {

    private val secureRandom = SecureRandom()
    private const val ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    private val USER_AGENTS = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UD1A.230803.041) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
        "Mozilla/5.0 (iPad; CPU OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/124.0.6367.88 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.2420.81",
        "NetForge/1.0 (Android; Axiom Collective Encrypted Tunnel Client)"
    )

    private fun generateNonce(length: Int = 16): String {
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            sb.append(ALPHANUM[secureRandom.nextInt(ALPHANUM.length)])
        }
        return sb.toString()
    }

    /**
     * Replaces placeholders in longest-first order and returns raw rendered string.
     */
    fun render(template: String, ctx: PayloadContext): String {
        val hostPort = "${ctx.host}:${ctx.port}"
        val frontHost = if (!ctx.frontHost.isNullOrBlank()) ctx.frontHost else ctx.host
        val realHost = ctx.host
        val sshUser = ctx.sshUser ?: ""
        val sshPass = ctx.sshPass ?: ""
        val protocol = if (ctx.tls) "https" else "http"
        val nonce = generateNonce(16)
        val ua = USER_AGENTS[secureRandom.nextInt(USER_AGENTS.size)]
        val crlf = "\r\n"
        val lf = "\n"
        val cr = "\r"
        val host = ctx.host
        val port = ctx.port.toString()

        // Placeholders sorted longest first:
        // [front_host] (12)
        // [host_port]  (11)
        // [real_host]  (11)
        // [protocol]   (10)
        // [ssh_user]   (10)
        // [ssh_pass]   (10)
        // [nonce]      (7)
        // [crlf]       (6)
        // [host]       (6)
        // [port]       (6)
        // [ua]         (4)
        // [lf]         (4)
        // [cr]         (4)

        var rendered = template
            .replace("[front_host]", frontHost)
            .replace("[host_port]", hostPort)
            .replace("[real_host]", realHost)
            .replace("[protocol]", protocol)
            .replace("[ssh_user]", sshUser)
            .replace("[ssh_pass]", sshPass)
            .replace("[nonce]", nonce)
            .replace("[crlf]", crlf)
            .replace("[host]", host)
            .replace("[port]", port)
            .replace("[ua]", ua)
            .replace("[lf]", lf)
            .replace("[cr]", cr)

        return rendered
    }

    /**
     * Returns UTF-8 byte array, with no extra trailing newline appended.
     */
    fun renderToBytes(template: String, ctx: PayloadContext): ByteArray {
        return render(template, ctx).toByteArray(Charsets.UTF_8)
    }
}
