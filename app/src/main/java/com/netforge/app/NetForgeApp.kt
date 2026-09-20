package com.netforge.app

import android.app.Application
import com.netforge.app.data.db.NetForgeDatabase
import com.netforge.app.data.prefs.DataStoreManager
import com.netforge.app.data.prefs.SecurePrefs
import com.netforge.app.data.repo.ProfileRepository
import com.netforge.app.logging.ConsoleBus

class NetForgeApp : Application() {

    val database by lazy { NetForgeDatabase.getInstance(this) }
    val securePrefs by lazy { SecurePrefs(this) }
    val dataStoreManager by lazy { DataStoreManager(this) }
    val repository by lazy { ProfileRepository(database.profileDao()) }

    override fun onCreate() {
        super.onCreate()
        ConsoleBus.info("System", "Running on ${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL}, Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        ConsoleBus.info("System", "Application Version: V 1.5 Build 37 - Rebuild by: Flex Net Team")
        ConsoleBus.info("System", "Device network: Active (Ready to route)")
        ConsoleBus.info("System", "Local IP: 10.8.0.2")
        ConsoleBus.info("System", "Notification channel active")
    }
}
