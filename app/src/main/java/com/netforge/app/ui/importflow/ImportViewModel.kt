package com.netforge.app.ui.importflow

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.data.file.NfgException
import com.netforge.app.data.file.NfgReader
import com.netforge.app.domain.model.Profile
import com.netforge.app.logging.ConsoleBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ImportState {
    object Idle : ImportState()
    data class NeedsPassphrase(val hint: String?, val attemptCount: Int, val isLockedOut: Boolean, val lockoutRemaining: Int = 0) : ImportState()
    data class Preview(val profile: Profile, val warningMessage: String?) : ImportState()
    data class Error(val message: String) : ImportState()
    object Success : ImportState()
}

class ImportViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository
    private val securePrefs = app.securePrefs
    private val dataStore = app.dataStoreManager

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private var loadedBytes: ByteArray? = null
    private var attempts = 0
    private var lockoutSeconds = 0

    fun loadFileFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    app.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                if (bytes == null || bytes.isEmpty()) {
                    _importState.value = ImportState.Error("Selected file is empty")
                    return@launch
                }
                processBytes(bytes)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to read file: ${e.message}")
            }
        }
    }

    fun loadSampleRawProfile() {
        viewModelScope.launch {
            try {
                val resId = app.resources.getIdentifier("sample", "raw", app.packageName)
                if (resId == 0) {
                    _importState.value = ImportState.Error("Sample profile asset not found")
                    return@launch
                }
                val bytes = withContext(Dispatchers.IO) {
                    app.resources.openRawResource(resId).use { it.readBytes() }
                }
                processBytes(bytes)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to load sample: ${e.message}")
            }
        }
    }

    fun processBytes(bytes: ByteArray) {
        loadedBytes = bytes
        if (!NfgReader.isNetForgeFile(bytes)) {
            _importState.value = ImportState.Error("Not a NetForge profile: Invalid file magic")
            return
        }
        attempts = 0
        _importState.value = ImportState.NeedsPassphrase(hint = null, attemptCount = 0, isLockedOut = false)
    }

    fun submitPassphrase(passphrase: String) {
        val bytes = loadedBytes ?: return
        if (lockoutSeconds > 0) return

        viewModelScope.launch {
            try {
                val profile = withContext(Dispatchers.IO) {
                    NfgReader.unseal(bytes, passphrase, attempts + 1)
                }

                // Decryption succeeded! Check policies
                when (val result = ImportEnforcer.checkPolicies(app, profile, securePrefs)) {
                    is EnforcementResult.Blocked -> {
                        _importState.value = ImportState.Error(result.reason)
                    }
                    is EnforcementResult.Warning -> {
                        _importState.value = ImportState.Preview(profile, result.message)
                    }
                    is EnforcementResult.Allowed -> {
                        _importState.value = ImportState.Preview(profile, null)
                    }
                }

            } catch (e: NfgException.IncorrectPassphrase) {
                attempts++
                if (attempts >= 3) {
                    startLockout()
                } else {
                    _importState.value = ImportState.NeedsPassphrase(
                        hint = "Passphrase failed. Try again.",
                        attemptCount = attempts,
                        isLockedOut = false
                    )
                }
            } catch (e: NfgException) {
                _importState.value = ImportState.Error(e.message ?: "Decryption error")
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to decrypt: ${e.message}")
            }
        }
    }

    private fun startLockout() {
        lockoutSeconds = 30
        viewModelScope.launch {
            while (lockoutSeconds > 0) {
                _importState.value = ImportState.NeedsPassphrase(
                    hint = "Too many failed attempts. Locked out for ${lockoutSeconds}s",
                    attemptCount = attempts,
                    isLockedOut = true,
                    lockoutRemaining = lockoutSeconds
                )
                delay(1000)
                lockoutSeconds--
            }
            attempts = 0
            _importState.value = ImportState.NeedsPassphrase(
                hint = "Lockout ended. You may try again.",
                attemptCount = 0,
                isLockedOut = false
            )
        }
    }

    fun confirmImport(profile: Profile) {
        viewModelScope.launch {
            // Save profile to database
            val id = repo.saveProfile(profile)
            dataStore.setActiveProfileId(id)

            // Update watermark limit in SecurePrefs
            val watermark = profile.advancedInfo.watermarkId.ifBlank { profile.name }
            securePrefs.incrementWatermarkImports(watermark)

            ConsoleBus.info("ImportViewModel", "Successfully imported profile '${profile.name}' (ID=$id)")
            _importState.value = ImportState.Success
        }
    }

    fun reset() {
        loadedBytes = null
        attempts = 0
        lockoutSeconds = 0
        _importState.value = ImportState.Idle
    }
}
