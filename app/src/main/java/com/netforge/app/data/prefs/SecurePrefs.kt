package com.netforge.app.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefs(context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "netforge_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences("netforge_fallback_prefs", Context.MODE_PRIVATE)
        }
    }

    fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String? = null): String? {
        return prefs.getString(key, default)
    }

    fun getWatermarkImports(watermarkId: String): Int {
        return prefs.getInt("wm_$watermarkId", 0)
    }

    fun incrementWatermarkImports(watermarkId: String) {
        val current = getWatermarkImports(watermarkId)
        prefs.edit().putInt("wm_$watermarkId", current + 1).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
