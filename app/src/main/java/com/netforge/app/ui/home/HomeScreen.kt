package com.netforge.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
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
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.ui.components.*
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateProfiles: () -> Unit,
    onNavigateConsole: () -> Unit,
    onNavigateBench: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateDeviceIdentity: () -> Unit,
    onNavigateShellAccess: () -> Unit,
    onNavigateSlowChannel: () -> Unit,
    onNavigateImport: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigatePayloadEditor: (Long) -> Unit,
    onPermissionRequired: () -> Unit
) {
    val context = LocalContext.current
    val phase by viewModel.phaseFlow.collectAsStateWithLifecycle()
    val metrics by viewModel.metricsFlow.collectAsStateWithLifecycle()
    val selectedNode by viewModel.selectedNode.collectAsStateWithLifecycle()
    val profile by viewModel.currentProfile.collectAsStateWithLifecycle()

    var showNodePicker by remember { mutableStateOf(false) }
    var showModePicker by remember { mutableStateOf(false) }
    var fineTuningExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NETFORGE",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = NetForgeChalk
                        )
                        Text(
                            text = profile.name,
                            style = Typography.labelSmall.copy(fontSize = 11.sp),
                            color = NetForgeAccent,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateConsole) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Console",
                            tint = NetForgeChalk
                        )
                    }
                    IconButton(onClick = onNavigateProfiles) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Profiles",
                            tint = NetForgeChalk
                        )
                    }
                    VaultMenu(
                        onNavigateSettings = onNavigateSettings,
                        onNavigateDeviceIdentity = onNavigateDeviceIdentity,
                        onNavigateShellAccess = onNavigateShellAccess,
                        onNavigateSlowChannel = onNavigateSlowChannel,
                        onNavigateImport = onNavigateImport,
                        onNavigateBench = onNavigateBench,
                        onNavigateAbout = onNavigateAbout,
                        onQuit = {
                            viewModel.stopTunnel(context)
                            (context as? android.app.Activity)?.finish()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NetForgeInk
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Announcement Banner
            if (profile.bannerInfo.enabled) {
                BannerPreview(banner = profile.bannerInfo)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Central Status Ring
            StatusRing(
                phase = phase,
                metrics = metrics,
                selectedNode = selectedNode,
                onClick = {
                    if (phase == TunnelPhase.Error) {
                        onNavigateConsole()
                    } else {
                        viewModel.toggleTunnel(context, onPermissionRequired)
                    }
                },
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Pill
            ActionPill(
                phase = phase,
                onClick = {
                    Haptics.medium(context)
                    viewModel.toggleTunnel(context, onPermissionRequired)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Metric Strip
            MetricStrip(metrics = metrics)

            Spacer(modifier = Modifier.height(20.dp))

            // Node Selection Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        Haptics.selection(context as android.view.View)
                        showNodePicker = true
                    }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = selectedNode.flagEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gateway Node",
                            style = Typography.labelSmall,
                            color = NetForgeSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedNode.name,
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = NetForgeChalk
                        )
                    }
                    Text(
                        text = "${selectedNode.latencyMs} ms",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NetForgeMoss
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selection Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        Haptics.selection(context as android.view.View)
                        showModePicker = true
                    }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NetForgeAccent.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = profile.mode.displayName.take(1),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NetForgeAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Transport Mode",
                            style = Typography.labelSmall,
                            color = NetForgeSlate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile.mode.displayName,
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = NetForgeChalk
                        )
                    }
                    Text(
                        text = "Change",
                        style = Typography.labelSmall.copy(color = NetForgeAccent),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fine Tuning Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { fineTuningExpanded = !fineTuningExpanded }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NetForgeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Fine Tuning & Protocols",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = NetForgeChalk,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (fineTuningExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = NetForgeSlate
                        )
                    }

                    AnimatedVisibility(visible = fineTuningExpanded) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Payload template shortcut
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NetForgePaper2)
                                    .clickable { onNavigatePayloadEditor(profile.id) }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = NetForgeAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Payload Template Editor",
                                        style = Typography.bodyMedium.copy(color = NetForgeChalk)
                                    )
                                }
                                Text(
                                    text = "Edit",
                                    style = Typography.labelSmall.copy(color = NetForgeAccent, fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // SNI
                            OutlinedTextField(
                                value = profile.sni,
                                onValueChange = { viewModel.updateProfile(profile.copy(sni = it)) },
                                label = { Text("TLS Server Name Indication (SNI)", color = NetForgeSlate) },
                                singleLine = true,
                                textStyle = MonoTextStyle.copy(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NetForgeAccent,
                                    unfocusedBorderColor = NetForgeBorder,
                                    focusedContainerColor = NetForgeInk,
                                    unfocusedContainerColor = NetForgeInk,
                                    focusedTextColor = NetForgeChalk,
                                    unfocusedTextColor = NetForgeChalk
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Front Host
                            OutlinedTextField(
                                value = profile.frontHost,
                                onValueChange = { viewModel.updateProfile(profile.copy(frontHost = it)) },
                                label = { Text("Front Host / CDN Domain", color = NetForgeSlate) },
                                singleLine = true,
                                textStyle = MonoTextStyle.copy(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NetForgeAccent,
                                    unfocusedBorderColor = NetForgeBorder,
                                    focusedContainerColor = NetForgeInk,
                                    unfocusedContainerColor = NetForgeInk,
                                    focusedTextColor = NetForgeChalk,
                                    unfocusedTextColor = NetForgeChalk
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // MTU Slider
                            Text(
                                text = "MTU Size: ${profile.mtu} bytes",
                                style = Typography.labelSmall,
                                color = NetForgeSlate
                            )
                            Slider(
                                value = profile.mtu.toFloat(),
                                onValueChange = { viewModel.updateProfile(profile.copy(mtu = it.toInt())) },
                                valueRange = 1200f..1500f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = NetForgeAccent,
                                    activeTrackColor = NetForgeAccent,
                                    inactiveTrackColor = NetForgeBorder
                                )
                            )

                            // Keepalive Slider
                            Text(
                                text = "Keepalive Interval: ${profile.keepalive}s",
                                style = Typography.labelSmall,
                                color = NetForgeSlate
                            )
                            Slider(
                                value = profile.keepalive.toFloat(),
                                onValueChange = { viewModel.updateProfile(profile.copy(keepalive = it.toInt())) },
                                valueRange = 5f..60f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = NetForgeAccent,
                                    activeTrackColor = NetForgeAccent,
                                    inactiveTrackColor = NetForgeBorder
                                )
                            )

                            // UDP Toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Enable UDP Forwarding",
                                    style = Typography.bodyMedium,
                                    color = NetForgeChalk
                                )
                                Switch(
                                    checked = profile.enableUdp,
                                    onCheckedChange = { viewModel.updateProfile(profile.copy(enableUdp = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NetForgeChalk,
                                        checkedTrackColor = NetForgeAccent,
                                        uncheckedThumbColor = NetForgeSlate,
                                        uncheckedTrackColor = NetForgeInk
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Sheets
        if (showNodePicker) {
            NodePickerSheet(
                selectedNodeId = selectedNode.id,
                onNodeSelected = { node -> viewModel.selectNode(node) },
                onDismiss = { showNodePicker = false }
            )
        }

        if (showModePicker) {
            ModePickerSheet(
                selectedMode = profile.mode,
                onModeSelected = { mode -> viewModel.selectMode(mode) },
                onDismiss = { showModePicker = false }
            )
        }
    }
}
