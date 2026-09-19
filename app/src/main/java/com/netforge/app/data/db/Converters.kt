package com.netforge.app.data.db

import androidx.room.TypeConverter
import com.netforge.app.domain.model.*

class Converters {

    @TypeConverter
    fun fromMode(mode: Mode): String = mode.name

    @TypeConverter
    fun toMode(name: String): Mode = try {
        Mode.valueOf(name)
    } catch (_: Exception) {
        Mode.Wrapped
    }

    @TypeConverter
    fun fromExpiryRule(rule: ExpiryRule): String {
        return "${rule.enabled}|${rule.type.name}|${rule.epochMs}|${rule.durationMs}|${rule.warningText}|${rule.mode.name}"
    }

    @TypeConverter
    fun toExpiryRule(raw: String?): ExpiryRule {
        if (raw.isNullOrBlank()) return ExpiryRule()
        val parts = raw.split("|")
        return try {
            ExpiryRule(
                enabled = parts[0].toBoolean(),
                type = ExpiryType.valueOf(parts[1]),
                epochMs = parts[2].toLong(),
                durationMs = parts[3].toLong(),
                warningText = parts[4],
                mode = ExpiryMode.valueOf(parts[5])
            )
        } catch (_: Exception) {
            ExpiryRule()
        }
    }

    @TypeConverter
    fun fromLimitRule(rule: LimitRule): String {
        return "${rule.enabled}|${rule.maxImports}|${rule.resetOn.name}|${rule.mode.name}"
    }

    @TypeConverter
    fun toLimitRule(raw: String?): LimitRule {
        if (raw.isNullOrBlank()) return LimitRule()
        val parts = raw.split("|")
        return try {
            LimitRule(
                enabled = parts[0].toBoolean(),
                maxImports = parts[1].toInt(),
                resetOn = LimitReset.valueOf(parts[2]),
                mode = LimitMode.valueOf(parts[3])
            )
        } catch (_: Exception) {
            LimitRule()
        }
    }

    @TypeConverter
    fun fromBindingRule(rule: BindingRule): String {
        return "${rule.bindDevice}|${rule.boundDeviceId}|${rule.blockRoot}|${rule.blockEmulator}|${rule.requireAppMatch}|${rule.expectedPackage}"
    }

    @TypeConverter
    fun toBindingRule(raw: String?): BindingRule {
        if (raw.isNullOrBlank()) return BindingRule()
        val parts = raw.split("|")
        return try {
            BindingRule(
                bindDevice = parts[0].toBoolean(),
                boundDeviceId = parts[1],
                blockRoot = parts[2].toBoolean(),
                blockEmulator = parts[3].toBoolean(),
                requireAppMatch = parts[4].toBoolean(),
                expectedPackage = parts.getOrNull(5) ?: "com.aistudio.netforge.cxtrqu"
            )
        } catch (_: Exception) {
            BindingRule()
        }
    }

    @TypeConverter
    fun fromLockInfo(lock: LockInfo): String {
        return "${lock.sealedWithPassphrase}|${lock.passphraseHint}|${lock.requireTwelveChars}|${lock.requireMixedAndSymbols}"
    }

    @TypeConverter
    fun toLockInfo(raw: String?): LockInfo {
        if (raw.isNullOrBlank()) return LockInfo()
        val parts = raw.split("|")
        return try {
            LockInfo(
                sealedWithPassphrase = parts[0].toBoolean(),
                passphraseHint = parts[1],
                requireTwelveChars = parts[2].toBoolean(),
                requireMixedAndSymbols = parts[3].toBoolean()
            )
        } catch (_: Exception) {
            LockInfo()
        }
    }

    @TypeConverter
    fun fromBannerInfo(banner: BannerInfo): String {
        return "${banner.enabled}|${banner.style.name}|${banner.message}|${banner.customColorHex}|${banner.durationSeconds}|${banner.buttonLabel}|${banner.buttonUrl}"
    }

    @TypeConverter
    fun toBannerInfo(raw: String?): BannerInfo {
        if (raw.isNullOrBlank()) return BannerInfo()
        val parts = raw.split("|")
        return try {
            BannerInfo(
                enabled = parts[0].toBoolean(),
                style = BannerStyle.valueOf(parts[1]),
                message = parts[2],
                customColorHex = parts[3],
                durationSeconds = parts[4].toInt(),
                buttonLabel = parts.getOrNull(5) ?: "",
                buttonUrl = parts.getOrNull(6) ?: ""
            )
        } catch (_: Exception) {
            BannerInfo()
        }
    }

    @TypeConverter
    fun fromAdvancedInfo(adv: AdvancedInfo): String {
        return "${adv.signatureTag}|${adv.watermarkId}|${adv.tamperProtection}"
    }

    @TypeConverter
    fun toAdvancedInfo(raw: String?): AdvancedInfo {
        if (raw.isNullOrBlank()) return AdvancedInfo()
        val parts = raw.split("|")
        return try {
            AdvancedInfo(
                signatureTag = parts[0],
                watermarkId = parts[1],
                tamperProtection = parts[2].toBoolean()
            )
        } catch (_: Exception) {
            AdvancedInfo()
        }
    }
}
