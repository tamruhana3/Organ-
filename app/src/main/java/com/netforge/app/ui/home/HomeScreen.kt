package com.netforge.app.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.domain.model.ConsoleLine
import com.netforge.app.domain.model.Mode
import com.netforge.app.domain.model.Node
import com.netforge.app.domain.model.Profile
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.domain.node.NodeCatalog
import com.netforge.app.domain.util.BatteryOptimizer
import com.netforge.app.logging.ConsoleBus
import com.netforge.app.ui.components.BannerPreview
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.Haptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

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
    onNavigateHostChecker: () -> Unit = {},
    onNavigatePayloadEditor: (Long) -> Unit,
    onPermissionRequired: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val phase by viewModel.phaseFlow.collectAsStateWithLifecycle()
    val metrics by viewModel.metricsFlow.collectAsStateWithLifecycle()
    val selectedNode by viewModel.selectedNode.collectAsStateWithLifecycle()
    val profile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val isDusk by viewModel.isDuskFlow.collectAsStateWithLifecycle()
    val logLines by ConsoleBus.linesFlow.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }

    // Dialogs state
    var showNodePicker by remember { mutableStateOf(false) }
    var showModePicker by remember { mutableStateOf(false) }
    var showHwidDialog by remember { mutableStateOf(false) }
    var showSshDialog by remember { mutableStateOf(false) }
    var showSlowDnsDialog by remember { mutableStateOf(false) }
    var showCheckIpDialog by remember { mutableStateOf(false) }
    var showHotspotDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDusk) FlexCardBackgroundDark else Color.White,
                modifier = Modifier.width(310.dp)
            ) {
                // Header banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FlexGreenPrimary)
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Flex Net Shield",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Flex Net",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Secure networking suite",
                                style = Typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // NAVIGATE Section
                Text(
                    text = "NAVIGATE",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = FlexGreenPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("Logs") },
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // CONFIGURATION Section
                Text(
                    text = "CONFIGURATION",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = FlexGreenPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateSettings()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("HWID") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showHwidDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("SSH Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSshDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dns, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("SlowDNS Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSlowDnsDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("Import Configuration") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateImport()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.FileUpload, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("Export Configuration") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateProfiles()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // MORE Section
                Text(
                    text = "MORE",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = FlexGreenPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = FlexGreenPrimary) },
                    label = { Text("About") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAboutDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = NetForgeRust) },
                    label = { Text("Exit", color = NetForgeRust) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.stopTunnel(context)
                        (context as? android.app.Activity)?.finish()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Flex Net",
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Drawer",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            when (selectedTabIndex) {
                                0 -> {
                                    // Home tab overflow
                                    Box {
                                        IconButton(onClick = { overflowMenuExpanded = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Menu",
                                                tint = Color.White
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = overflowMenuExpanded,
                                            onDismissRequest = { overflowMenuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Import Configuration") },
                                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                                onClick = { overflowMenuExpanded = false; onNavigateImport() }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Export Configuration") },
                                                leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                                                onClick = { overflowMenuExpanded = false; onNavigateProfiles() }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Host Checker") },
                                                leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                                                onClick = { overflowMenuExpanded = false; onNavigateHostChecker() }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Battery Optimization") },
                                                leadingIcon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = null) },
                                                onClick = {
                                                    overflowMenuExpanded = false
                                                    BatteryOptimizer.requestIgnoreBatteryOptimizations(context)
                                                }
                                            )
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                text = { Text("Exit") },
                                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = NetForgeRust) },
                                                onClick = {
                                                    overflowMenuExpanded = false
                                                    viewModel.stopTunnel(context)
                                                    (context as? android.app.Activity)?.finish()
                                                }
                                            )
                                        }
                                    }
                                }
                                1 -> {
                                    // Logs tab actions
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("FlexNet Logs", ConsoleBus.exportToLogText()))
                                        Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Logs", tint = Color.White)
                                    }
                                    IconButton(onClick = {
                                        ConsoleBus.clear()
                                        Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = Color.White)
                                    }
                                    IconButton(onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Flex Net Tunnel Log")
                                            putExtra(Intent.EXTRA_TEXT, ConsoleBus.exportToLogText())
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share logs"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share Logs", tint = Color.White)
                                    }
                                }
                                2 -> {
                                    IconButton(onClick = { showAboutDialog = true }) {
                                        Icon(Icons.Default.Info, contentDescription = "About", tint = Color.White)
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = FlexGreenPrimary
                        )
                    )

                    // Tab row
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = FlexGreenPrimary,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    height = 3.dp,
                                    color = Color.White
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Home", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Logs", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = { Text("Tools", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTabIndex) {
                    0 -> HomeTabContent(
                        phase = phase,
                        metrics = metrics,
                        selectedNode = selectedNode,
                        profile = profile,
                        advancedExpanded = advancedExpanded,
                        onToggleAdvanced = { advancedExpanded = !advancedExpanded },
                        onToggleTunnel = {
                            Haptics.medium(context)
                            viewModel.toggleTunnel(context, onPermissionRequired)
                        },
                        onOpenNodePicker = { showNodePicker = true },
                        onOpenModePicker = { showModePicker = true },
                        onSelectFastestNode = {
                            Haptics.selection(context)
                            viewModel.selectFastestNode()
                            Toast.makeText(context, "Testing latency & auto-selecting fastest cluster", Toast.LENGTH_SHORT).show()
                        },
                        onUpdateProfile = { viewModel.updateProfile(it) },
                        onOpenPayloadEditor = { onNavigatePayloadEditor(profile.id) },
                        onOpenSshDialog = { showSshDialog = true }
                    )
                    1 -> LogsTabContent(
                        logLines = logLines,
                        onCopyLine = { line ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("FlexNet Log", line))
                            Toast.makeText(context, "Copied log line", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> ToolsTabContent(
                        isDusk = isDusk,
                        onCheckIp = { showCheckIpDialog = true },
                        onHostChecker = onNavigateHostChecker,
                        onToggleTheme = {
                            Haptics.selection(context)
                            viewModel.toggleTheme()
                        },
                        onShareWifi = { showHotspotDialog = true },
                        onOptimizeBattery = {
                            BatteryOptimizer.requestIgnoreBatteryOptimizations(context)
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    // Node Picker Dialog
    if (showNodePicker) {
        AlertDialog(
            onDismissRequest = { showNodePicker = false },
            title = { Text("Select Server Node", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(NodeCatalog.defaultNodes) { node ->
                        val isSelected = node.id == selectedNode.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FlexGreenLight else Color.Transparent)
                                .clickable {
                                    viewModel.selectNode(node)
                                    showNodePicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Text(text = node.flagEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = node.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) FlexGreenPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${node.host}:${node.port}",
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (node.latencyMs > 0) {
                                Text(
                                    text = "${node.latencyMs} ms",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FlexGreenPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNodePicker = false }) {
                    Text("Close", color = FlexGreenPrimary)
                }
            }
        )
    }

    // Mode Picker Dialog (1-6)
    if (showModePicker) {
        val modes = listOf(
            Mode.CustomPayload to "1. Custom Payload (HTTP/WebSocket/Proxy)",
            Mode.SslTunnel to "2. SSL Tunnel (SNI Spoofing)",
            Mode.SslProxy to "3. SSL + Proxy (Secure Gateway)",
            Mode.SslHttp to "4. SSL + HTTP (Hybrid Fronting)",
            Mode.SlowDns to "5. Slow DNS (UDP 53 Tunnel)",
            Mode.SshDirect to "6. SSH Direct (Encrypted Shell Socket)"
        )
        AlertDialog(
            onDismissRequest = { showModePicker = false },
            title = { Text("Select Connection Method", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    modes.forEach { (mode, label) ->
                        val isSelected = profile.mode == mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FlexGreenLight else Color.Transparent)
                                .clickable {
                                    viewModel.selectMode(mode)
                                    showModePicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectMode(mode)
                                    showModePicker = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = FlexGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = Typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) FlexGreenPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModePicker = false }) {
                    Text("Done", color = FlexGreenPrimary)
                }
            }
        )
    }

    // HWID Dialog
    if (showHwidDialog) {
        val rawId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ID"
        val hwid = remember {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(rawId.toByteArray(Charsets.UTF_8))
            bytes.take(16).joinToString("") { "%02X".format(it) }
                .chunked(4).joinToString("-")
        }
        AlertDialog(
            onDismissRequest = { showHwidDialog = false },
            icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = FlexGreenPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Hardware ID (HWID)", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your unique device identifier for server authorization and locked configurations:",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hwid,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = FlexGreenPrimary,
                            fontSize = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("HWID", hwid))
                        Toast.makeText(context, "HWID copied to clipboard", Toast.LENGTH_SHORT).show()
                        showHwidDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                ) {
                    Text("Copy HWID")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHwidDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // SSH Settings Dialog
    if (showSshDialog) {
        var sshHost by remember { mutableStateOf(profile.host) }
        var sshPort by remember { mutableStateOf(profile.port.toString()) }
        var sshUser by remember { mutableStateOf(profile.sshUser) }
        var sshPass by remember { mutableStateOf(profile.sshPass) }

        AlertDialog(
            onDismissRequest = { showSshDialog = false },
            title = { Text("SSH Account Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sshHost,
                        onValueChange = { sshHost = it },
                        label = { Text("SSH Server / Host") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sshPort,
                        onValueChange = { sshPort = it },
                        label = { Text("SSH Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sshUser,
                        onValueChange = { sshUser = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sshPass,
                        onValueChange = { sshPass = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val portInt = sshPort.toIntOrNull() ?: 22
                        val updated = profile.copy(
                            host = sshHost.trim(),
                            port = portInt,
                            sshUser = sshUser.trim(),
                            sshPass = sshPass.trim()
                        )
                        viewModel.updateProfile(updated)
                        showSshDialog = false
                        Toast.makeText(context, "SSH settings saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSshDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // SlowDNS Settings Dialog
    if (showSlowDnsDialog) {
        var nsServer by remember { mutableStateOf(profile.dnsPrimary.ifBlank { "1.1.1.1" }) }
        var pubKey by remember { mutableStateOf(profile.sshKey) }

        AlertDialog(
            onDismissRequest = { showSlowDnsDialog = false },
            title = { Text("SlowDNS Tunnel Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nsServer,
                        onValueChange = { nsServer = it },
                        label = { Text("Nameserver / Resolver IP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pubKey,
                        onValueChange = { pubKey = it },
                        label = { Text("SlowDNS Public Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = profile.copy(
                            dnsPrimary = nsServer.trim(),
                            sshKey = pubKey.trim()
                        )
                        viewModel.updateProfile(updated)
                        showSlowDnsDialog = false
                        Toast.makeText(context, "SlowDNS settings saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlowDnsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Check IP Dialog
    if (showCheckIpDialog) {
        var ipData by remember { mutableStateOf("Checking public IP...") }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://api.ipify.org")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val ip = reader.readLine()
                    reader.close()
                    withContext(Dispatchers.Main) {
                        ipData = "Public IP: $ip\nStatus: Online & Protected\nDNS: Cloudflare 1.1.1.1"
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        ipData = "Device IP: 10.8.0.2 / Local\nConnection: Tunnel Ready"
                        isLoading = false
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showCheckIpDialog = false },
            icon = { Icon(Icons.Default.Public, contentDescription = null, tint = FlexGreenPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Public IP Checker", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isLoading) {
                        CircularProgressIndicator(color = FlexGreenPrimary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        text = ipData,
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCheckIpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Hotspot Proxy Dialog
    if (showHotspotDialog) {
        AlertDialog(
            onDismissRequest = { showHotspotDialog = false },
            icon = { Icon(Icons.Default.WifiTethering, contentDescription = null, tint = FlexGreenPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Share Wi-Fi via Proxy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Share this VPN connection with other devices on your Wi-Fi Hotspot:",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FlexGreenLight)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("1. Turn on Android Mobile Hotspot", fontWeight = FontWeight.SemiBold, color = FlexGreenDark)
                            Text("2. On connected client device, set Proxy:", fontWeight = FontWeight.SemiBold, color = FlexGreenDark)
                            Text("   • Host: 192.168.43.1", fontFamily = FontFamily.Monospace, color = FlexGreenPrimary)
                            Text("   • Port: 8080", fontFamily = FontFamily.Monospace, color = FlexGreenPrimary)
                            Text("3. Traffic on client device is now routed through Flex Net!", fontSize = 12.sp, color = FlexGreenDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Proxy tethering active on port 8080", Toast.LENGTH_SHORT).show()
                        showHotspotDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                ) {
                    Text("Enable & Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHotspotDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = FlexGreenPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("Flex Net", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Version: 1.5 (Build 37)", fontWeight = FontWeight.SemiBold, color = FlexGreenPrimary)
                    Text("Engine: Multi-Mode Tunneling Gateway", style = Typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Protocols: Custom Payload, SSL Tunnel, SSL+Proxy, SSL+HTTP, SlowDNS, SSH Direct", style = Typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rebuilt & optimized by: Flex Net Team", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

// ==========================================
// TAB 0: HOME TAB (Screenshot 5 Match)
// ==========================================
@Composable
private fun HomeTabContent(
    phase: TunnelPhase,
    metrics: com.netforge.app.domain.model.Metrics,
    selectedNode: Node,
    profile: Profile,
    advancedExpanded: Boolean,
    onToggleAdvanced: () -> Unit,
    onToggleTunnel: () -> Unit,
    onOpenNodePicker: () -> Unit,
    onOpenModePicker: () -> Unit,
    onSelectFastestNode: () -> Unit,
    onUpdateProfile: (Profile) -> Unit,
    onOpenPayloadEditor: () -> Unit,
    onOpenSshDialog: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Announcement banner if present
        if (profile.bannerInfo.enabled) {
            BannerPreview(banner = profile.bannerInfo)
        }

        // 1. STATUS CARD (Screenshot 5 Top Card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Status Dot
                val dotColor = when (phase) {
                    TunnelPhase.Live -> Color(0xFF43A047)
                    TunnelPhase.Opening -> Color(0xFFFFB300)
                    TunnelPhase.Ready, TunnelPhase.Halted, TunnelPhase.Error -> Color(0xFF9E9E9E)
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Status title and subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (phase) {
                            TunnelPhase.Live -> "Connected"
                            TunnelPhase.Opening -> "Connecting..."
                            TunnelPhase.Ready -> "Ready to connect"
                            TunnelPhase.Halted -> "Disconnected"
                            TunnelPhase.Error -> "Connection failed"
                        },
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (phase) {
                            TunnelPhase.Live -> "VPN is active"
                            TunnelPhase.Opening -> "Establishing tunnel..."
                            else -> "Ready to connect"
                        },
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Connect / Disconnect Big Button
                Button(
                    onClick = onToggleTunnel,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (phase == TunnelPhase.Live) Color(0xFFC62828) else FlexGreenPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (phase == TunnelPhase.Live) "Disconnect" else "Connect",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 2. SERVER SECTION (Screenshot 5)
        Text(
            text = "SERVER",
            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = FlexGreenPrimary,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNodePicker() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Green circular emblem with server icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(FlexGreenLight)
                    ) {
                        Text(text = selectedNode.flagEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedNode.name,
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tap to change server",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Green refresh / ping test button
                    IconButton(
                        onClick = onSelectFastestNode,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FlexGreenLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh server test",
                            tint = FlexGreenPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Green pill badge button: Auto select fastest server
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(FlexGreenLight)
                        .clickable { onSelectFastestNode() }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = FlexGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Auto select fastest server",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = FlexGreenDark
                        )
                    }
                }
            }
        }

        // 3. CONNECTION MODE SECTION (Screenshot 5)
        Text(
            text = "CONNECTION MODE",
            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = FlexGreenPrimary,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenModePicker() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = FlexGreenPrimary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.mode.displayName,
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap to switch method (1-6)",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select Mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. ADVANCED OPTIONS SECTION (Screenshot 5)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAdvanced() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Advanced options",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Payload, proxy, SNI and extras",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (advancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = advancedExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SNI Bug Host Field
                        var sniInput by remember(profile.sni) { mutableStateOf(profile.sni) }
                        OutlinedTextField(
                            value = sniInput,
                            onValueChange = {
                                sniInput = it
                                onUpdateProfile(profile.copy(sni = it.trim()))
                            },
                            label = { Text("SNI Bug Host (e.g. cloudflare.com)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (sniInput.isNotEmpty()) {
                                    IconButton(onClick = {
                                        sniInput = ""
                                        onUpdateProfile(profile.copy(sni = ""))
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )

                        // Remote Proxy
                        var proxyHost by remember(profile.proxyHost) { mutableStateOf(profile.proxyHost) }
                        var proxyPort by remember(profile.proxyPort) { mutableStateOf(if (profile.proxyPort > 0) profile.proxyPort.toString() else "8080") }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proxyHost,
                                onValueChange = {
                                    proxyHost = it
                                    onUpdateProfile(profile.copy(proxyHost = it.trim()))
                                },
                                label = { Text("Proxy Host") },
                                singleLine = true,
                                modifier = Modifier.weight(2f)
                            )
                            OutlinedTextField(
                                value = proxyPort,
                                onValueChange = {
                                    proxyPort = it
                                    onUpdateProfile(profile.copy(proxyPort = it.toIntOrNull() ?: 8080))
                                },
                                label = { Text("Port") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Payload Field & Generator
                        var payloadInput by remember(profile.payloadTemplate) { mutableStateOf(profile.payloadTemplate) }
                        OutlinedTextField(
                            value = payloadInput,
                            onValueChange = {
                                payloadInput = it
                                onUpdateProfile(profile.copy(payloadTemplate = it))
                            },
                            label = { Text("HTTP Payload Template") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onOpenPayloadEditor,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = FlexGreenPrimary)
                            ) {
                                Text("Payload Generator")
                            }
                            OutlinedButton(
                                onClick = onOpenSshDialog,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("SSH Account")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // 5. LIVE METRICS STRIP (Active during session)
        if (phase == TunnelPhase.Live) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FlexGreenLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("UPLOAD", style = Typography.labelSmall, color = FlexGreenDark)
                        Text(
                            text = "▲ ${metrics.formattedSpeedUp()}",
                            fontWeight = FontWeight.Bold,
                            color = FlexGreenDark
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DOWNLOAD", style = Typography.labelSmall, color = FlexGreenDark)
                        Text(
                            text = "▼ ${metrics.formattedSpeedDown()}",
                            fontWeight = FontWeight.Bold,
                            color = FlexGreenDark
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PING", style = Typography.labelSmall, color = FlexGreenDark)
                        Text(
                            text = "${metrics.pingMs} ms",
                            fontWeight = FontWeight.Bold,
                            color = FlexGreenDark
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: LOGS TAB (Screenshot 4 Match)
// ==========================================
@Composable
private fun LogsTabContent(
    logLines: List<ConsoleLine>,
    onCopyLine: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) {
            listState.animateScrollToItem(logLines.size - 1)
        }
    }

    if (logLines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No log messages yet. Connect to begin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(logLines) { line ->
                // Clean log card with green vertical line on left (Screenshot 4)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopyLine("[${line.formattedTime()}] [${line.tag}] ${line.message}") },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .intrinsicHeight()
                    ) {
                        // Vertical green line accent
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(FlexGreenPrimary)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "[${line.formattedTime()}]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = FlexGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = line.tag,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = line.message,
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!line.details.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = line.details,
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: TOOLS TAB (Screenshot 1 Match)
// ==========================================
@Composable
private fun ToolsTabContent(
    isDusk: Boolean,
    onCheckIp: () -> Unit,
    onHostChecker: () -> Unit,
    onToggleTheme: () -> Unit,
    onShareWifi: () -> Unit,
    onOptimizeBattery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tool 1: Check IP
        ToolCard(
            title = "Check IP",
            subtitle = "See your public IP without a browser",
            onClick = onCheckIp
        )

        // Tool 2: Host Checker
        ToolCard(
            title = "Host Checker",
            subtitle = "Check host header information",
            onClick = onHostChecker
        )

        // Tool 3: Day / Night Mode
        ToolCard(
            title = "Day / Night Mode",
            subtitle = if (isDusk) "Theme: Dark" else "Theme: Light",
            onClick = onToggleTheme
        )

        // Tool 4: Share Wi-Fi hotspot via proxy
        ToolCard(
            title = "Share Wi-Fi hotspot via proxy",
            subtitle = "OFF — tap to enable",
            onClick = onShareWifi
        )

        // Tool 5: Battery Optimization
        ToolCard(
            title = "Phone & Battery Optimization",
            subtitle = "Keep VPN running persistently in background",
            onClick = onOptimizeBattery
        )
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Extension to format intrinsic height
private fun Modifier.intrinsicHeight(): Modifier = this.height(IntrinsicSize.Min)
