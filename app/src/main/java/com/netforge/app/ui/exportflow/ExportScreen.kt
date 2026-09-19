package com.netforge.app.ui.exportflow

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.domain.model.ExpiryMode
import com.netforge.app.domain.model.LimitMode
import com.netforge.app.ui.components.PasswordField
import com.netforge.app.ui.components.StrengthMeter
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val pass by viewModel.passphrase.collectAsStateWithLifecycle()
    val confirmPass by viewModel.confirmPassphrase.collectAsStateWithLifecycle()
    val hint by viewModel.passphraseHint.collectAsStateWithLifecycle()

    val lockProfile by viewModel.lockProfile.collectAsStateWithLifecycle()
    val blockRoot by viewModel.blockRoot.collectAsStateWithLifecycle()
    val blockEmulator by viewModel.blockEmulator.collectAsStateWithLifecycle()
    val bindDevice by viewModel.bindDevice.collectAsStateWithLifecycle()
    val boundDeviceId by viewModel.boundDeviceId.collectAsStateWithLifecycle()

    val expiryEnabled by viewModel.expiryEnabled.collectAsStateWithLifecycle()
    val expiryDays by viewModel.expiryDays.collectAsStateWithLifecycle()

    val limitEnabled by viewModel.limitEnabled.collectAsStateWithLifecycle()
    val maxImports by viewModel.maxImports.collectAsStateWithLifecycle()

    val exportedBytes by viewModel.exportedBytes.collectAsStateWithLifecycle()
    val sha256Fingerprint by viewModel.sha256Fingerprint.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val ok = viewModel.writeToUri(uri)
            if (ok) {
                Haptics.success(context)
            }
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Export Encrypted .nfg",
                        style = Typography.titleLarge,
                        color = NetForgeChalk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NetForgeChalk
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "STEP 1: VAULT PASSPHRASE",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = NetForgeAccent
            )

            PasswordField(
                value = pass,
                onValueChange = { viewModel.passphrase.value = it },
                label = "Export Passphrase"
            )

            StrengthMeter(passphrase = pass)

            PasswordField(
                value = confirmPass,
                onValueChange = { viewModel.confirmPassphrase.value = it },
                label = "Confirm Passphrase"
            )

            if (confirmPass.isNotEmpty() && pass != confirmPass) {
                Text(
                    text = "Passphrases do not match",
                    style = Typography.bodySmall,
                    color = NetForgeRust
                )
            }

            OutlinedTextField(
                value = hint,
                onValueChange = { viewModel.passphraseHint.value = it },
                label = { Text("Passphrase Hint (Optional)", color = NetForgeSlate) },
                singleLine = true,
                colors = customExportFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "STEP 2: PROTECTION RULES",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = NetForgeAccent
            )

            // Policy checkboxes
            ExportSwitchRow(
                label = "Lock Profile",
                description = "Make profile read-only after import",
                checked = lockProfile,
                onCheckedChange = { viewModel.lockProfile.value = it }
            )

            ExportSwitchRow(
                label = "Block Rooted Devices",
                description = "Prevent import if su binary or magisk detected",
                checked = blockRoot,
                onCheckedChange = { viewModel.blockRoot.value = it }
            )

            ExportSwitchRow(
                label = "Block Emulators",
                description = "Disallow importing on virtualized Android OS",
                checked = blockEmulator,
                onCheckedChange = { viewModel.blockEmulator.value = it }
            )

            ExportSwitchRow(
                label = "Bind to Target Device",
                description = "Cryptographically lock to a specific device hash",
                checked = bindDevice,
                onCheckedChange = { viewModel.bindDevice.value = it }
            )

            if (bindDevice) {
                OutlinedTextField(
                    value = boundDeviceId,
                    onValueChange = { viewModel.boundDeviceId.value = it },
                    label = { Text("Target Device ID", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customExportFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ExportSwitchRow(
                label = "Set Expiration Limit",
                description = "Deactivate profile after set duration",
                checked = expiryEnabled,
                onCheckedChange = { viewModel.expiryEnabled.value = it }
            )

            if (expiryEnabled) {
                Text(
                    text = "Expires in: $expiryDays days",
                    style = Typography.bodySmall,
                    color = NetForgeChalk
                )
                Slider(
                    value = expiryDays.toFloat(),
                    onValueChange = { viewModel.expiryDays.value = it.toInt() },
                    valueRange = 1f..90f,
                    colors = SliderDefaults.colors(thumbColor = NetForgeAccent, activeTrackColor = NetForgeAccent)
                )
            }

            ExportSwitchRow(
                label = "Set Import Usage Limit",
                description = "Cap total number of permitted imports",
                checked = limitEnabled,
                onCheckedChange = { viewModel.limitEnabled.value = it }
            )

            if (limitEnabled) {
                Text(
                    text = "Max imports: $maxImports",
                    style = Typography.bodySmall,
                    color = NetForgeChalk
                )
                Slider(
                    value = maxImports.toFloat(),
                    onValueChange = { viewModel.maxImports.value = it.toInt() },
                    valueRange = 1f..50f,
                    colors = SliderDefaults.colors(thumbColor = NetForgeAccent, activeTrackColor = NetForgeAccent)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Generate Button
            Button(
                onClick = {
                    Haptics.medium(context)
                    viewModel.generateSealedNfg()
                },
                enabled = !isExporting && pass.isNotBlank() && pass == confirmPass,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExporting) "Encrypting Vault..." else "Generate Sealed .nfg",
                    fontWeight = FontWeight.Bold
                )
            }

            // Export Result Card
            if (exportedBytes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NetForgePaper)
                        .border(1.dp, NetForgeMoss, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Vault Sealed Successfully",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NetForgeMoss
                        )

                        Text(
                            text = "Size: ${exportedBytes!!.size} bytes",
                            style = MonoTextStyle.copy(fontSize = 12.sp),
                            color = NetForgeSlate
                        )

                        if (sha256Fingerprint != null) {
                            Text(
                                text = "SHA-256 Fingerprint:\n$sha256Fingerprint",
                                style = MonoTextStyle.copy(fontSize = 10.sp),
                                color = NetForgeChalk
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val defaultName = "${profile?.name ?: "netforge"}.nfg"
                                    saveFileLauncher.launch(defaultName)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save File")
                            }

                            OutlinedButton(
                                onClick = {
                                    val temp = viewModel.createTempShareFile()
                                    if (temp != null) {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", temp)
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/octet-stream"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share NetForge Vault"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NetForgeChalk)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ExportSwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NetForgeChalk)
            Text(text = description, style = Typography.bodySmall, color = NetForgeSlate)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NetForgeChalk,
                checkedTrackColor = NetForgeAccent,
                uncheckedThumbColor = NetForgeSlate,
                uncheckedTrackColor = NetForgePaper
            )
        )
    }
}

@Composable
private fun customExportFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NetForgeAccent,
    unfocusedBorderColor = NetForgeBorder,
    focusedContainerColor = NetForgeInk,
    unfocusedContainerColor = NetForgeInk,
    focusedTextColor = NetForgeChalk,
    unfocusedTextColor = NetForgeChalk
)
