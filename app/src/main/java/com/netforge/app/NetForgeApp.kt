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
        ConsoleBus.info("NetForgeApp", "NetForge application initialized")
    }
}
