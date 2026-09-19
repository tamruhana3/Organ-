package com.netforge.app.data.file

import com.netforge.app.crypto.Argon2Kdf
import com.netforge.app.crypto.Seal
import com.netforge.app.domain.model.Profile
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NfgWriter {

    private val secureRandom = SecureRandom()

    fun buildIniText(profile: Profile): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val isoDate = sdf.format(Date(profile.createdAt))

        return buildString {
            appendLine("[meta]")
            appendLine("name = ${profile.name}")
            appendLine("author = ${profile.author}")
            appendLine("note = ${profile.note}")
            appendLine("created = $isoDate")
            appendLine("version = 1")
            appendLine()

            appendLine("[node]")
            appendLine("host = ${profile.host}")
            appendLine("port = ${profile.port}")
            appendLine("mode = ${profile.mode.name}")
            appendLine()

            appendLine("[wrapping]")
            appendLine("sni = ${profile.sni}")
            appendLine("front_host = ${profile.frontHost}")
            appendLine("template = ${profile.payloadTemplate}")
            appendLine("ssh_user = ${profile.sshUser}")
            appendLine("ssh_pass = ${profile.sshPass}")
            appendLine("ssh_key = ${profile.sshKey}")
            appendLine("proxy = ${profile.proxyHost}")
            appendLine("proxy_port = ${profile.proxyPort}")
            appendLine()

            appendLine("[resolver]")
            appendLine("primary = ${profile.dnsPrimary}")
            appendLine("secondary = ${profile.dnsSecondary}")
            appendLine("custom = ${profile.customDns}")
            appendLine()

            appendLine("[tuning]")
            appendLine("keepalive = ${profile.keepalive}")
            appendLine("mtu = ${profile.mtu}")
            appendLine("udp = ${profile.enableUdp}")
            appendLine("verbose = true")
            appendLine()

            appendLine("[expiry]")
            appendLine("enabled = ${profile.expiryRule.enabled}")
            appendLine("type = ${profile.expiryRule.type.name}")
            appendLine("epoch_ms = ${profile.expiryRule.epochMs}")
            appendLine("duration_ms = ${profile.expiryRule.durationMs}")
            appendLine("warning = ${profile.expiryRule.warningText}")
            appendLine("mode = ${profile.expiryRule.mode.name}")
            appendLine()

            appendLine("[limit]")
            appendLine("enabled = ${profile.limitRule.enabled}")
            appendLine("max = ${profile.limitRule.maxImports}")
            appendLine("reset = ${profile.limitRule.resetOn.name}")
            appendLine("mode = ${profile.limitRule.mode.name}")
            appendLine()

            appendLine("[binding]")
            appendLine("bind_device = ${profile.bindingRule.bindDevice}")
            appendLine("device_id = ${profile.bindingRule.boundDeviceId}")
            appendLine("block_root = ${profile.bindingRule.blockRoot}")
            appendLine("block_emulator = ${profile.bindingRule.blockEmulator}")
            appendLine("require_app_match = ${profile.bindingRule.requireAppMatch}")
            appendLine("expected_package = ${profile.bindingRule.expectedPackage}")
            appendLine()

            appendLine("[lock]")
            appendLine("hint = ${profile.lockInfo.passphraseHint}")
            appendLine("require_twelve = ${profile.lockInfo.requireTwelveChars}")
            appendLine("require_mixed = ${profile.lockInfo.requireMixedAndSymbols}")
            appendLine()

            appendLine("[banner]")
            appendLine("enabled = ${profile.bannerInfo.enabled}")
            appendLine("style = ${profile.bannerInfo.style.name}")
            appendLine("message = ${profile.bannerInfo.message}")
            appendLine("color = ${profile.bannerInfo.customColorHex}")
            appendLine("duration_ms = ${profile.bannerInfo.durationSeconds * 1000}")
            appendLine("button_label = ${profile.bannerInfo.buttonLabel}")
            appendLine("button_url = ${profile.bannerInfo.buttonUrl}")
            appendLine()

            appendLine("[advanced]")
            appendLine("signature_tag = ${profile.advancedInfo.signatureTag}")
            appendLine("watermark_id = ${profile.advancedInfo.watermarkId}")
            appendLine("tamper_protection = ${profile.advancedInfo.tamperProtection}")
        }
    }

    fun seal(profile: Profile, passphrase: String): ByteArray {
        val iniText = buildIniText(profile)
        val plaintextBytes = iniText.toByteArray(Charsets.UTF_8)
        val gzipped = Seal.gzipCompress(plaintextBytes)

        val header = NfgFormat.buildHeader()
        val salt = ByteArray(NfgFormat.SALT_SIZE)
        secureRandom.nextBytes(salt)

        val nonce = ByteArray(NfgFormat.NONCE_SIZE)
        secureRandom.nextBytes(nonce)

        val key = Argon2Kdf.deriveKey(passphrase, salt)
        val ciphertext = try {
            Seal.encryptAesGcm(gzipped, key, nonce, header)
        } finally {
            Seal.zeroOut(key)
        }

        val out = ByteArrayOutputStream()
        out.write(header)
        out.write(salt)
        out.write(nonce)
        out.write(ciphertext)
        return out.toByteArray()
    }
}
