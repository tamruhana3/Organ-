package com.netforge.app.ui.bench

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.netforge.app.ui.components.BenchCard
import com.netforge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchScreen(
    onNavigateBack: () -> Unit,
    onNavigateWhereAmI: () -> Unit,
    onNavigatePingline: () -> Unit,
    onNavigateTimekeeper: () -> Unit,
    onNavigateBridge: () -> Unit,
    onNavigateFlowmeter: () -> Unit,
    onNavigateTraceback: () -> Unit,
    onNavigateHostChecker: () -> Unit = {}
) {
    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Diagnostics Bench",
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "NETWORK VERIFICATION & TELEMETRY",
                style = Typography.labelSmall,
                color = NetForgeSlate
            )

            BenchCard(
                title = "Host Checker",
                description = "Probe bug hosts, test SNI spoofing, inspect HTTP headers, status codes and response latency.",
                icon = Icons.Default.Language,
                accentColor = NetForgeAccent,
                onClick = onNavigateHostChecker
            )

            BenchCard(
                title = "Where Am I?",
                description = "Query active public egress IP, geographical routing, and check for DNS leakage.",
                icon = Icons.Default.Public,
                accentColor = NetForgeMoss,
                onClick = onNavigateWhereAmI
            )

            BenchCard(
                title = "Pingline",
                description = "Measure real-time RTT latency, variance jitter, and packet round-trip timing.",
                icon = Icons.Default.Speed,
                accentColor = NetForgeAccent,
                onClick = onNavigatePingline
            )

            BenchCard(
                title = "Timekeeper",
                description = "Inspect session stability index, uptime counters, and tunnel duty cycles.",
                icon = Icons.Default.Timer,
                accentColor = NetForgeAmber,
                onClick = onNavigateTimekeeper
            )

            BenchCard(
                title = "Bridge",
                description = "Probe custom TCP sockets, test TLS handshakes, and inspect remote SSL certificates.",
                icon = Icons.Default.Security,
                accentColor = NetForgeEmber,
                onClick = onNavigateBridge
            )

            BenchCard(
                title = "Flowmeter",
                description = "Real-time canvas throughput graph with instantaneous upload and download speeds.",
                icon = Icons.Default.ShowChart,
                accentColor = NetForgeAccent,
                onClick = onNavigateFlowmeter
            )

            BenchCard(
                title = "Traceback",
                description = "Enumerate network interfaces, verify TUN descriptor routing, and calculate MTU limits.",
                icon = Icons.Default.Route,
                accentColor = NetForgeRust,
                onClick = onNavigateTraceback
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
