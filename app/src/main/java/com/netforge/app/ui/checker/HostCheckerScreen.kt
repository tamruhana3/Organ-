package com.netforge.app.ui.checker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netforge.app.ui.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostCheckerScreen(
    onNavigateBack: () -> Unit,
    viewModel: HostCheckerViewModel = viewModel()
) {
    val context = LocalContext.current
    val targetUrl by viewModel.targetUrl.collectAsState()
    val requestMethod by viewModel.requestMethod.collectAsState()
    val injectionMode by viewModel.injectionMode.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()
    val result by viewModel.result.collectAsState()

    var showApplyToast by remember { mutableStateOf(false) }

    val presetHosts = listOf(
        "fast.com",
        "cloudflare.com",
        "zoom.us",
        "speedtest.net",
        "1.1.1.1",
        "m.tiktok.com",
        "web.whatsapp.com"
    )

    val methods = listOf("GET", "CONNECT", "HEAD", "POST", "OPTIONS")
    val injectionModes = listOf("Direct (Normal)", "Front Inject", "Back Inject")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Host Checker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Test bug hosts, SNI & proxy status",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Haptics.selection(context)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick preset bug hosts
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK BUG HOST PRESETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetHosts.forEach { host ->
                        FilterChip(
                            selected = targetUrl.contains(host),
                            onClick = {
                                Haptics.selection(context)
                                viewModel.targetUrl.value = "https://$host"
                            },
                            label = { Text(host) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Target URL Input
            OutlinedTextField(
                value = targetUrl,
                onValueChange = { viewModel.targetUrl.value = it },
                label = { Text("Bug Host / URL") },
                placeholder = { Text("https://fast.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Method & Injection Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Method Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "METHOD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        methods.forEach { m ->
                            FilterChip(
                                selected = requestMethod == m,
                                onClick = {
                                    Haptics.selection(context)
                                    viewModel.requestMethod.value = m
                                },
                                label = { Text(m, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Injection Mode Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "INJECTION MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    injectionModes.forEach { mode ->
                        FilterChip(
                            selected = injectionMode == mode,
                            onClick = {
                                Haptics.selection(context)
                                viewModel.injectionMode.value = mode
                            },
                            label = { Text(mode, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Check Button
            Button(
                onClick = {
                    Haptics.selection(context)
                    viewModel.checkHost()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isChecking && targetUrl.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probing Host...")
                } else {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check Host Status", fontWeight = FontWeight.Bold)
                }
            }

            // Results Card
            AnimatedVisibility(visible = result != null) {
                result?.let { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header Row: Status badge & RTT
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statusColor = when {
                                    res.statusCode in 200..299 -> Color(0xFF10B981) // Green
                                    res.statusCode in 300..399 -> Color(0xFFF59E0B) // Amber
                                    res.statusCode == 101 -> Color(0xFF8B5CF6) // Purple
                                    else -> Color(0xFFEF4444) // Red
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(statusColor.copy(alpha = 0.2f))
                                            .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (res.statusCode > 0) "${res.statusCode} ${res.statusMessage}" else "FAILED",
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "${res.totalTimeMs} ms",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider()

                            // Diagnostics Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "RESOLVED IP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = res.resolvedIp.ifBlank { "N/A" },
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "DNS LOOKUP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${res.dnsLookupMs} ms",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (res.error != null) {
                                Text(
                                    text = "Error: ${res.error}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Response Headers
                            if (res.headers.isNotEmpty()) {
                                Text(
                                    text = "RESPONSE HEADERS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        res.headers.entries.take(8).forEach { (k, v) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "$k:",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = v,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Action: Apply host to Profile
                            FilledTonalButton(
                                onClick = {
                                    Haptics.selection(context)
                                    viewModel.applyToCurrentProfile {
                                        showApplyToast = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply as SNI to Active Profile")
                            }

                            if (showApplyToast) {
                                Text(
                                    text = "✓ Host applied to active profile successfully!",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
