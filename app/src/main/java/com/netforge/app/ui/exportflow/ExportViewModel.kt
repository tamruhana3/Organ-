package com.netforge.app.ui.exportflow

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netforge.app.NetForgeApp
import com.netforge.app.data.file.NfgWriter
import com.netforge.app.domain.model.*
import com.netforge.app.logging.ConsoleBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class ExportViewModel(
    application: Application,
    private val profileId: Long
) : AndroidViewModel(application) {

    private val app = application as NetForgeApp
    private val repo = app.repository

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    // Passphrase state
    val passphrase = MutableStateFlow("")
    val confirmPassphrase = MutableStateFlow("")
    val passphraseHint = MutableStateFlow("")

    // Protection flags
    val lockProfile = MutableStateFlow(true)
    val blockRoot = MutableStateFlow(false)
    val blockEmulator = MutableStateFlow(false)
    val bindDevice = MutableStateFlow(false)
    val boundDeviceId = MutableStateFlow("")
    val requireAppMatch = MutableStateFlow(false)

    // Expiry rule
    val expiryEnabled = MutableStateFlow(false)
    val expiryDays = MutableStateFlow(30)
    val expiryMode = MutableStateFlow(ExpiryMode.Block)

    // Limit rule
    val limitEnabled = MutableStateFlow(false)
    val maxImports = MutableStateFlow(5)
    val limitMode = MutableStateFlow(LimitMode.Block)

    // Banner
    val bannerEnabled = MutableStateFlow(false)
    val bannerMessage = MutableStateFlow("")
    val bannerStyle = MutableStateFlow(BannerStyle.Info)
    val bannerButtonLabel = MutableStateFlow("")
    val bannerButtonUrl = MutableStateFlow("")

    // Export result
    private val _exportedBytes = MutableStateFlow<ByteArray?>(null)
    val exportedBytes: StateFlow<ByteArray?> = _exportedBytes.asStateFlow()

    private val _sha256Fingerprint = MutableStateFlow<String?>(null)
    val sha256Fingerprint: StateFlow<String?> = _sha256Fingerprint.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    init {
        viewModelScope.launch {
            val p = repo.getProfileById(profileId)
            _profile.value = p
        }
    }

    fun generateSealedNfg() {
        val current = _profile.value ?: return
        if (passphrase.value.isBlank() || passphrase.value != confirmPassphrase.value) {
            return
        }

        _isExporting.value = true
        viewModelScope.launch {
            try {
                val updated = current.copy(
                    lockInfo = current.lockInfo.copy(sealedWithPassphrase = true),
                    bindingRule = BindingRule(
                        blockRoot = blockRoot.value,
                        blockEmulator = blockEmulator.value,
                        bindDevice = bindDevice.value,
                        boundDeviceId = boundDeviceId.value,
                        requireAppMatch = requireAppMatch.value,
                        expectedPackage = app.packageName
                    ),
                    expiryRule = ExpiryRule(
                        enabled = expiryEnabled.value,
                        epochMs = if (expiryEnabled.value) System.currentTimeMillis() + (expiryDays.value * 86400000L) else 0L,
                        mode = expiryMode.value,
                        warningText = "Profile expired after ${expiryDays.value} days."
                    ),
                    limitRule = LimitRule(
                        enabled = limitEnabled.value,
                        maxImports = maxImports.value,
                        mode = limitMode.value
                    ),
                    bannerInfo = BannerInfo(
                        enabled = bannerEnabled.value,
                        message = bannerMessage.value,
                        style = bannerStyle.value,
                        buttonLabel = bannerButtonLabel.value,
                        buttonUrl = bannerButtonUrl.value
                    )
                )

                val bytes = withContext(Dispatchers.IO) {
                    NfgWriter.seal(updated, passphrase.value)
                }
                _exportedBytes.value = bytes

                // Calculate SHA-256 fingerprint
                val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
                val hex = digest.joinToString("") { "%02x".format(it) }
                _sha256Fingerprint.value = hex

                ConsoleBus.info("ExportViewModel", "Generated sealed .nfg for '${current.name}' (${bytes.size} bytes)")
            } catch (e: Exception) {
                ConsoleBus.error("ExportViewModel", "Export failed: ${e.message}")
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun writeToUri(uri: Uri): Boolean {
        val bytes = _exportedBytes.value ?: return false
        return try {
            app.contentResolver.openOutputStream(uri)?.use {
                it.write(bytes)
                it.flush()
            }
            true
        } catch (e: Exception) {
            ConsoleBus.error("ExportViewModel", "Failed to write file to uri: ${e.message}")
            false
        }
    }

    fun createTempShareFile(): File? {
        val bytes = _exportedBytes.value ?: return null
        return try {
            val file = File(app.cacheDir, "shared_profile.nfg")
            file.writeBytes(bytes)
            file
        } catch (e: Exception) {
            null
        }
    }
}
