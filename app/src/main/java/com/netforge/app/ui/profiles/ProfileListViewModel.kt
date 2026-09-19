package com.netforge.app.ui.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.domain.model.Mode
import com.netforge.app.domain.model.Profile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository
    private val dataStore = app.dataStoreManager

    val profiles: StateFlow<List<Profile>> = repo.allProfilesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val activeProfileId: StateFlow<Long> = dataStore.activeProfileIdFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        1L
    )

    fun setActiveProfile(profile: Profile) {
        viewModelScope.launch {
            dataStore.setActiveProfileId(profile.id)
        }
    }

    fun toggleFavorite(profile: Profile) {
        viewModelScope.launch {
            repo.toggleFavorite(profile.id, profile.isFavorite)
        }
    }

    fun renameProfile(id: Long, newName: String) {
        viewModelScope.launch {
            repo.renameProfile(id, newName)
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repo.deleteProfile(id)
        }
    }

    fun createNewProfile(): Long {
        var newId = 0L
        val newProfile = Profile(
            name = "New Custom Profile",
            author = "NetForge user",
            mode = Mode.Wrapped,
            host = "node-us-east.netforge.internal",
            port = 443
        )
        viewModelScope.launch {
            newId = repo.saveProfile(newProfile)
            dataStore.setActiveProfileId(newId)
        }
        return newId
    }
}
