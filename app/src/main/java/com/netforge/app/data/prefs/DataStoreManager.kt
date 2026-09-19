package com.netforge.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore by preferencesDataStore(name = "netforge_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_ONBOARDED = booleanPreferencesKey("onboarded")
        val KEY_ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        val KEY_SELECTED_NODE_ID = stringPreferencesKey("selected_node_id")
        val KEY_KILL_SWITCH = booleanPreferencesKey("kill_switch")
        val KEY_BYPASS_LAN = booleanPreferencesKey("bypass_lan")
        val KEY_CHIME_ON_LIVE = booleanPreferencesKey("chime_on_live")
        val KEY_FOLLOW_CONSOLE = booleanPreferencesKey("follow_console")
    }

    val themeFlow: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_THEME] ?: "Dusk" }

    val isDuskThemeFlow: Flow<Boolean> = themeFlow.map { it != "Dawn" }

    val onboardedFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ONBOARDED] ?: false }

    val hasCompletedOnboardingFlow: Flow<Boolean> = onboardedFlow

    val activeProfileIdFlow: Flow<Long> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ACTIVE_PROFILE_ID] ?: 1L }

    val selectedNodeIdFlow: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SELECTED_NODE_ID] ?: "node-us-east" }

    val killSwitchFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_KILL_SWITCH] ?: false }

    val bypassLanFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_BYPASS_LAN] ?: true }

    val chimeOnLiveFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_CHIME_ON_LIVE] ?: false }

    val followConsoleFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FOLLOW_CONSOLE] ?: true }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setDuskTheme(isDusk: Boolean) {
        setTheme(if (isDusk) "Dusk" else "Dawn")
    }

    suspend fun setOnboarded(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDED] = done }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        setOnboarded(done)
    }

    suspend fun setActiveProfileId(id: Long) {
        context.dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = id }
    }

    suspend fun setSelectedNodeId(nodeId: String) {
        context.dataStore.edit { it[KEY_SELECTED_NODE_ID] = nodeId }
    }

    suspend fun setKillSwitch(enabled: Boolean) {
        context.dataStore.edit { it[KEY_KILL_SWITCH] = enabled }
    }

    suspend fun setBypassLan(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BYPASS_LAN] = enabled }
    }

    suspend fun setChimeOnLive(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CHIME_ON_LIVE] = enabled }
    }

    suspend fun setFollowConsole(enabled: Boolean) {
        context.dataStore.edit { it[KEY_FOLLOW_CONSOLE] = enabled }
    }
}
