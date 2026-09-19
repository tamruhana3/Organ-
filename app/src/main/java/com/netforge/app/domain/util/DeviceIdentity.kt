package com.netforge.app.domain.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

object DeviceIdentity {

    private var cachedId: String? = null

    /**
     * Computes a deterministic 32-character hexadecimal identifier derived from
     * SHA-256(ANDROID_ID + Build.FINGERPRINT).
     */
    fun getDeviceId(context: Context): String {
        cachedId?.let { return it }
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_android_id"
        } catch (_: Exception) {
            "fallback_id"
        }
        val fingerprint = Build.FINGERPRINT ?: "generic_fingerprint"
        val raw = "$androidId:$fingerprint"

        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        val id32 = hex.substring(0, 32)
        cachedId = id32
        return id32
    }

    fun getDeviceSummary(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})"
    }
}
