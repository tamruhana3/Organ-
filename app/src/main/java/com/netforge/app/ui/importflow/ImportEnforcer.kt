package com.netforge.app.ui.importflow

import android.content.Context
import com.netforge.app.data.prefs.SecurePrefs
import com.netforge.app.domain.model.ExpiryMode
import com.netforge.app.domain.model.LimitMode
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.util.DeviceIdentity
import com.netforge.app.domain.util.EmulatorChecker
import com.netforge.app.domain.util.RootChecker

sealed class EnforcementResult {
    object Allowed : EnforcementResult()
    data class Warning(val message: String) : EnforcementResult()
    data class Blocked(val reason: String) : EnforcementResult()
}

object ImportEnforcer {

    fun checkPolicies(context: Context, profile: Profile, securePrefs: SecurePrefs): EnforcementResult {
        // 1. Expiry Check
        if (profile.expiryRule.enabled && profile.expiryRule.isExpired()) {
            val warning = profile.expiryRule.warningText.ifBlank { "This profile has expired." }
            return if (profile.expiryRule.mode == ExpiryMode.Block) {
                EnforcementResult.Blocked("Profile expired: $warning")
            } else {
                EnforcementResult.Warning("Warning: $warning")
            }
        }

        // 2. Limit Check
        if (profile.limitRule.enabled) {
            val watermark = profile.advancedInfo.watermarkId.ifBlank { profile.name }
            val count = securePrefs.getWatermarkImports(watermark)
            if (count >= profile.limitRule.maxImports) {
                return if (profile.limitRule.mode == LimitMode.Block) {
                    EnforcementResult.Blocked("Import limit exceeded (${profile.limitRule.maxImports} max imports allowed).")
                } else {
                    EnforcementResult.Warning("Warning: Import limit reached (${profile.limitRule.maxImports}).")
                }
            }
        }

        // 3. Device Binding Check
        if (profile.bindingRule.bindDevice && profile.bindingRule.boundDeviceId.isNotBlank()) {
            val currentDeviceId = DeviceIdentity.getDeviceId(context)
            if (!currentDeviceId.equals(profile.bindingRule.boundDeviceId, ignoreCase = true)) {
                return EnforcementResult.Blocked("Device lock violation: This profile is cryptographically bound to another device.")
            }
        }

        // 4. Root Detection Check
        if (profile.bindingRule.blockRoot && RootChecker.isDeviceRooted()) {
            return EnforcementResult.Blocked("Security policy: This profile cannot be imported on rooted devices.")
        }

        // 5. Emulator Detection Check
        if (profile.bindingRule.blockEmulator && EmulatorChecker.isEmulator()) {
            return EnforcementResult.Blocked("Security policy: This profile cannot be imported in an emulator environment.")
        }

        // 6. App Package Match Check
        if (profile.bindingRule.requireAppMatch) {
            val expected = profile.bindingRule.expectedPackage
            if (expected.isNotBlank() && context.packageName != expected) {
                return EnforcementResult.Blocked("App identity mismatch: Expected $expected, got ${context.packageName}")
            }
        }

        return EnforcementResult.Allowed
    }
}
