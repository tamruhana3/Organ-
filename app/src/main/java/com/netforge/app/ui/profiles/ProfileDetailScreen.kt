package com.netforge.app.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.domain.model.Mode
import com.netforge.app.ui.components.ModePickerSheet
import com.netforge.app.ui.components.PasswordField
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    viewModel: ProfileDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigatePayloadEditor: (Long) -> Unit,
    onNavigateExport: (Long) -> Unit
) {
    val profileState by viewModel.profile.collectAsStateWithLifecycle()
    val testResult by viewModel.testResult.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTesting.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showModePicker by remember { mutableStateOf(false) }

    val profile = profileState

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = profile?.name ?: "Profile Details",
                        style = Typography.titleLarge,
                        color = NetForgeChalk,
                        maxLines = 1
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
                actions = {
                    if (profile != null) {
                        IconButton(onClick = { onNavigateExport(profile.id) }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export .nfg",
                                tint = NetForgeAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        }
    ) { padding ->
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NetForgeAccent)
            }
        } else {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Identity
                Text("IDENTITY", style = Typography.labelSmall, color = NetForgeSlate)

                OutlinedTextField(
                    value = profile.name,
                    onValueChange = { viewModel.updateProfile(profile.copy(name = it)) },
                    label = { Text("Profile Name", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profile.author,
                    onValueChange = { viewModel.updateProfile(profile.copy(author = it)) },
                    label = { Text("Author", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profile.note,
                    onValueChange = { viewModel.updateProfile(profile.copy(note = it)) },
                    label = { Text("Note / Instructions", color = NetForgeSlate) },
                    maxLines = 3,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Network Endpoint & Mode
                Text("TRANSPORT ENDPOINT", style = Typography.labelSmall, color = NetForgeSlate)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = profile.host,
                        onValueChange = { viewModel.updateProfile(profile.copy(host = it)) },
                        label = { Text("Server Host", color = NetForgeSlate) },
                        singleLine = true,
                        colors = customFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2.5f)
                    )

                    OutlinedTextField(
                        value = profile.port.toString(),
                        onValueChange = {
                            val p = it.toIntOrNull() ?: profile.port
                            viewModel.updateProfile(profile.copy(port = p))
                        },
                        label = { Text("Port", color = NetForgeSlate) },
                        singleLine = true,
                        colors = customFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Mode button
                OutlinedButton(
                    onClick = { showModePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NetForgeChalk)
                ) {
                    Text("Protocol Mode: ${profile.mode.displayName}", fontWeight = FontWeight.SemiBold)
                }

                // Test Connectivity Button
                Button(
                    onClick = { viewModel.testConnectivity() },
                    enabled = !isTesting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NetForgePaper2)
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = NetForgeMoss)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTesting) "Testing Socket..." else "Probe Server Reachability", color = NetForgeChalk)
                }

                if (testResult != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (testResult!!.startsWith("Reachable")) NetForgeMoss.copy(alpha = 0.15f) else NetForgeRust.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = testResult!!,
                            style = MonoTextStyle.copy(fontSize = 12.sp),
                            color = if (testResult!!.startsWith("Reachable")) NetForgeMoss else NetForgeRust
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Authentication
                Text("AUTHENTICATION (SSH)", style = Typography.labelSmall, color = NetForgeSlate)

                OutlinedTextField(
                    value = profile.sshUser,
                    onValueChange = { viewModel.updateProfile(profile.copy(sshUser = it)) },
                    label = { Text("SSH Username", color = NetForgeSlate) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                PasswordField(
                    value = profile.sshPass,
                    onValueChange = { viewModel.updateProfile(profile.copy(sshPass = it)) },
                    label = "SSH Password"
                )

                OutlinedTextField(
                    value = profile.sshKey,
                    onValueChange = { viewModel.updateProfile(profile.copy(sshKey = it)) },
                    label = { Text("SSH Private Key (Optional)", color = NetForgeSlate) },
                    maxLines = 3,
                    textStyle = MonoTextStyle.copy(fontSize = 11.sp),
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Payload template shortcut
                Button(
                    onClick = { onNavigatePayloadEditor(profile.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = NetForgeChalk)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Payload Editor", color = NetForgeChalk, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showModePicker && profile != null) {
            ModePickerSheet(
                selectedMode = profile.mode,
                onModeSelected = { mode ->
                    viewModel.updateProfile(profile.copy(mode = mode))
                },
                onDismiss = { showModePicker = false }
            )
        }
    }
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NetForgeAccent,
    unfocusedBorderColor = NetForgeBorder,
    focusedContainerColor = NetForgeInk,
    unfocusedContainerColor = NetForgeInk,
    focusedTextColor = NetForgeChalk,
    unfocusedTextColor = NetForgeChalk
)
