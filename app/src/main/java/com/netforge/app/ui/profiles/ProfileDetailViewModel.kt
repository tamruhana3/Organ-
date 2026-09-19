package com.netforge.app.ui.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.domain.model.Profile
import com.netforge.app.logging.ConsoleBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class ProfileDetailViewModel(
    application: Application,
    private val profileId: Long
) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    init {
        viewModelScope.launch {
            val p = repo.getProfileById(profileId)
            _profile.value = p
        }
    }

    fun updateProfile(updated: Profile) {
        _profile.value = updated
        viewModelScope.launch {
            repo.saveProfile(updated)
        }
    }

    fun testConnectivity() {
        val current = _profile.value ?: return
        _isTesting.value = true
        _testResult.value = null

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val socket = Socket()
                    val t0 = System.currentTimeMillis()
                    socket.connect(InetSocketAddress(current.host, current.port), 5000)
                    val rtt = System.currentTimeMillis() - t0
                    socket.close()
                    ConsoleBus.info("ProfileDetail", "Endpoint reachable: ${current.host}:${current.port} in ${rtt}ms")
                    "Reachable in ${rtt}ms"
                } catch (e: Exception) {
                    ConsoleBus.error("ProfileDetail", "Connection test failed: ${e.message}")
                    "Failed: ${e.message}"
                }
            }
            _testResult.value = result
            _isTesting.value = false
        }
    }
}
