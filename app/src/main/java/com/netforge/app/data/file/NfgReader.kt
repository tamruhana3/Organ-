package com.netforge.app.data.file

import com.netforge.app.crypto.Argon2Kdf
import com.netforge.app.crypto.Seal
import com.netforge.app.domain.model.*
import java.io.BufferedReader
import java.io.StringReader

sealed class NfgException(message: String) : Exception(message) {
    class InvalidMagic : NfgException("Not a NetForge profile")
    class UnsupportedVersion(val version: Byte) : NfgException("Profile created by a newer build")
    class IncorrectPassphrase(val attempt: Int, val hint: String? = null) : NfgException("Incorrect passphrase")
    class CorruptedPayload(cause: Throwable? = null) : NfgException("Profile file is corrupted")
}

object NfgReader {

    fun isNetForgeFile(bytes: ByteArray): Boolean {
        if (bytes.size < NfgFormat.HEADER_SIZE) return false
        for (i in 0 until 4) {
            if (bytes[i] != NfgFormat.MAGIC[i]) return false
        }
        return true
    }

    fun unseal(bytes: ByteArray, passphrase: String, attemptCount: Int = 1): Profile {
        if (!isNetForgeFile(bytes)) {
            throw NfgException.InvalidMagic()
        }

        val version = bytes[4]
        if (version > NfgFormat.VERSION_1) {
            throw NfgException.UnsupportedVersion(version)
        }

        val header = bytes.copyOfRange(0, NfgFormat.HEADER_SIZE)
        val saltStart = NfgFormat.HEADER_SIZE
        val saltEnd = saltStart + NfgFormat.SALT_SIZE
        val nonceStart = saltEnd
        val nonceEnd = nonceStart + NfgFormat.NONCE_SIZE

        if (bytes.size < nonceEnd + NfgFormat.TAG_SIZE) {
            throw NfgException.CorruptedPayload()
        }

        val salt = bytes.copyOfRange(saltStart, saltEnd)
        val nonce = bytes.copyOfRange(nonceStart, nonceEnd)
        val ciphertext = bytes.copyOfRange(nonceEnd, bytes.size)

        val key = Argon2Kdf.deriveKey(passphrase, salt)
        val decryptedGzip = try {
            Seal.decryptAesGcm(ciphertext, key, nonce, header)
        } catch (e: Exception) {
            throw NfgException.IncorrectPassphrase(attemptCount)
        } finally {
            Seal.zeroOut(key)
        }

        val plaintextBytes = try {
            Seal.gzipDecompress(decryptedGzip)
        } catch (e: Exception) {
            throw NfgException.CorruptedPayload(e)
        }

        val iniString = String(plaintextBytes, Charsets.UTF_8)
        return parseIniText(iniString)
    }

    fun parseIniText(ini: String): Profile {
        val sections = mutableMapOf<String, MutableMap<String, String>>()
        var currentSection = ""

        BufferedReader(StringReader(ini)).useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length - 1).trim().lowercase()
                    sections.putIfAbsent(currentSection, mutableMapOf())
                } else if (line.contains("=")) {
                    val parts = line.split("=", limit = 2)
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    sections[currentSection]?.put(key, value)
                }
            }
        }

        val meta = sections["meta"] ?: emptyMap()
        val node = sections["node"] ?: emptyMap()
        val wrapping = sections["wrapping"] ?: emptyMap()
        val resolver = sections["resolver"] ?: emptyMap()
        val tuning = sections["tuning"] ?: emptyMap()
        val expiry = sections["expiry"] ?: emptyMap()
        val limit = sections["limit"] ?: emptyMap()
        val binding = sections["binding"] ?: emptyMap()
        val lock = sections["lock"] ?: emptyMap()
        val banner = sections["banner"] ?: emptyMap()
        val advanced = sections["advanced"] ?: emptyMap()

        val mode = try {
            Mode.valueOf(node["mode"] ?: "Wrapped")
        } catch (_: Exception) {
            Mode.Wrapped
        }

        val expiryType = try {
            ExpiryType.valueOf(expiry["type"] ?: "Duration")
        } catch (_: Exception) {
            ExpiryType.Duration
        }

        val expiryMode = try {
            ExpiryMode.valueOf(expiry["mode"] ?: "Block")
        } catch (_: Exception) {
            ExpiryMode.Block
        }

        val limitReset = try {
            LimitReset.valueOf(limit["reset"] ?: "Never")
        } catch (_: Exception) {
            LimitReset.Never
        }

        val limitMode = try {
            LimitMode.valueOf(limit["mode"] ?: "Block")
        } catch (_: Exception) {
            LimitMode.Block
        }

        val bannerStyle = try {
            BannerStyle.valueOf(banner["style"] ?: "Info")
        } catch (_: Exception) {
            BannerStyle.Info
        }

        return Profile(
            name = meta["name"] ?: "Imported Profile",
            author = meta["author"] ?: "NetForge user",
            note = meta["note"] ?: "",
            host = node["host"] ?: "127.0.0.1",
            port = node["port"]?.toIntOrNull() ?: 443,
            mode = mode,
            sni = wrapping["sni"] ?: "",
            frontHost = wrapping["front_host"] ?: "",
            payloadTemplate = wrapping["template"] ?: "",
            sshUser = wrapping["ssh_user"] ?: "",
            sshPass = wrapping["ssh_pass"] ?: "",
            sshKey = wrapping["ssh_key"] ?: "",
            proxyHost = wrapping["proxy"] ?: "",
            proxyPort = wrapping["proxy_port"]?.toIntOrNull() ?: 3128,
            dnsPrimary = resolver["primary"] ?: "1.1.1.1",
            dnsSecondary = resolver["secondary"] ?: "1.0.0.1",
            customDns = resolver["custom"] ?: "",
            keepalive = tuning["keepalive"]?.toIntOrNull() ?: 15,
            mtu = tuning["mtu"]?.toIntOrNull() ?: 1500,
            enableUdp = tuning["udp"]?.toBooleanStrictOrNull() ?: true,
            expiryRule = ExpiryRule(
                enabled = expiry["enabled"]?.toBooleanStrictOrNull() ?: false,
                type = expiryType,
                epochMs = expiry["epoch_ms"]?.toLongOrNull() ?: 0L,
                durationMs = expiry["duration_ms"]?.toLongOrNull() ?: 86400000L,
                warningText = expiry["warning"] ?: "This profile has expired.",
                mode = expiryMode
            ),
            limitRule = LimitRule(
                enabled = limit["enabled"]?.toBooleanStrictOrNull() ?: false,
                maxImports = limit["max"]?.toIntOrNull() ?: 3,
                resetOn = limitReset,
                mode = limitMode
            ),
            bindingRule = BindingRule(
                bindDevice = binding["bind_device"]?.toBooleanStrictOrNull() ?: false,
                boundDeviceId = binding["device_id"] ?: "",
                blockRoot = binding["block_root"]?.toBooleanStrictOrNull() ?: false,
                blockEmulator = binding["block_emulator"]?.toBooleanStrictOrNull() ?: false,
                requireAppMatch = binding["require_app_match"]?.toBooleanStrictOrNull() ?: false,
                expectedPackage = binding["expected_package"] ?: "com.aistudio.netforge.cxtrqu"
            ),
            lockInfo = LockInfo(
                sealedWithPassphrase = true,
                passphraseHint = lock["hint"] ?: "",
                requireTwelveChars = lock["require_twelve"]?.toBooleanStrictOrNull() ?: false,
                requireMixedAndSymbols = lock["require_mixed"]?.toBooleanStrictOrNull() ?: false
            ),
            bannerInfo = BannerInfo(
                enabled = banner["enabled"]?.toBooleanStrictOrNull() ?: false,
                style = bannerStyle,
                message = banner["message"] ?: "",
                customColorHex = banner["color"] ?: "#7C5CFF",
                durationSeconds = (banner["duration_ms"]?.toLongOrNull() ?: 5000L).toInt() / 1000,
                buttonLabel = banner["button_label"] ?: "",
                buttonUrl = banner["button_url"] ?: ""
            ),
            advancedInfo = AdvancedInfo(
                signatureTag = advanced["signature_tag"] ?: "",
                watermarkId = advanced["watermark_id"] ?: "",
                tamperProtection = advanced["tamper_protection"]?.toBooleanStrictOrNull() ?: true
            )
        )
    }
}
