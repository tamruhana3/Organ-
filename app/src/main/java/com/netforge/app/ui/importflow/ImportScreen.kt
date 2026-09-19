package com.netforge.app.ui.importflow

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.ui.components.BannerPreview
import com.netforge.app.ui.components.PasswordField
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: ImportViewModel,
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit
) {
    val state by viewModel.importState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.loadFileFromUri(uri)
        }
    }

    var passphraseInput by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is ImportState.Success) {
            Haptics.success(context)
            onImportComplete()
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Import .nfg Vault",
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val s = state) {
                is ImportState.Idle -> {
                    Spacer(modifier = Modifier.height(20.dp))

                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = NetForgeAccent,
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "Select Encrypted Vault",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NetForgeChalk
                    )

                    Text(
                        text = "Choose an authentic .nfg file protected by Argon2id and AES-256-GCM encryption.",
                        style = Typography.bodyMedium,
                        color = NetForgeSlate,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Browse Files (.nfg)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadSampleRawProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NetForgeChalk)
                    ) {
                        Text("Load Sample Profile (netforge-demo)", color = NetForgeChalk)
                    }
                }

                is ImportState.NeedsPassphrase -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NetForgePaper)
                            .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = NetForgeAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Passphrase Required",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NetForgeChalk
                                )
                            }

                            Text(
                                text = "Enter the encryption passphrase used when this profile was sealed.",
                                style = Typography.bodySmall,
                                color = NetForgeSlate
                            )

                            if (s.hint != null) {
                                Text(
                                    text = s.hint,
                                    style = Typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (s.isLockedOut) NetForgeRust else NetForgeAmber
                                )
                            }

                            PasswordField(
                                value = passphraseInput,
                                onValueChange = { passphraseInput = it },
                                label = "Passphrase"
                            )

                            Button(
                                onClick = {
                                    Haptics.medium(context)
                                    viewModel.submitPassphrase(passphraseInput)
                                },
                                enabled = !s.isLockedOut && passphraseInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (s.isLockedOut) "Locked Out (${s.lockoutRemaining}s)" else "Decrypt & Verify",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is ImportState.Preview -> {
                    val p = s.profile

                    if (s.warningMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NetForgeAmber.copy(alpha = 0.15f))
                                .border(1.dp, NetForgeAmber, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = NetForgeAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.warningMessage,
                                    style = Typography.bodySmall,
                                    color = NetForgeAmber
                                )
                            }
                        }
                    }

                    if (p.bannerInfo.enabled) {
                        BannerPreview(banner = p.bannerInfo)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NetForgePaper)
                            .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = p.name,
                                style = Typography.titleLarge,
                                color = NetForgeChalk
                            )

                            Text(
                                text = "By ${p.author}",
                                style = Typography.bodyMedium,
                                color = NetForgeSlate
                            )

                            if (p.note.isNotBlank()) {
                                Text(
                                    text = p.note,
                                    style = Typography.bodySmall,
                                    color = NetForgeSlate
                                )
                            }

                            HorizontalDivider(color = NetForgeBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Protocol Mode:", style = Typography.bodySmall, color = NetForgeSlate)
                                Text(p.mode.displayName, style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = NetForgeAccent)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Endpoint:", style = Typography.bodySmall, color = NetForgeSlate)
                                Text("${p.host}:${p.port}", style = MonoTextStyle.copy(fontSize = 12.sp), color = NetForgeChalk)
                            }

                            if (p.sni.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("SNI Domain:", style = Typography.bodySmall, color = NetForgeSlate)
                                    Text(p.sni, style = MonoTextStyle.copy(fontSize = 12.sp), color = NetForgeChalk)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.confirmImport(p) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import to Vault", fontWeight = FontWeight.Bold)
                    }
                }

                is ImportState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NetForgeRust.copy(alpha = 0.15f))
                            .border(1.dp, NetForgeRust, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Import Rejected",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NetForgeRust
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = s.message,
                                style = Typography.bodyMedium,
                                color = NetForgeChalk
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NetForgePaper2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Try Another File", color = NetForgeChalk)
                    }
                }

                is ImportState.Success -> {
                    // Handled in LaunchedEffect
                }
            }
        }
    }
}
