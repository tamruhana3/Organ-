package com.netforge.app.domain.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.netforge.app.logging.ConsoleBus

object BatteryOptimizer {

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (e: Exception) {
            ConsoleBus.debug("BatteryOptimizer", "Error checking battery optimization: ${e.message}")
            false
        }
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ConsoleBus.info("BatteryOptimizer", "Requested direct battery optimization whitelist")
        } catch (e: Exception) {
            ConsoleBus.warn("BatteryOptimizer", "Direct battery prompt failed, falling back to settings: ${e.message}")
            openBatteryOptimizationSettings(context)
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            ConsoleBus.error("BatteryOptimizer", "Failed to launch battery settings: ${e.message}")
        }
    }
}
