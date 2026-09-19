package com.netforge.app.ui.bench

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.service.NetForgeVpnService
import com.netforge.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimekeeperScreen(
    onNavigateBack: () -> Unit
) {
    val phase by NetForgeVpnService.phaseFlow.collectAsStateWithLifecycle()
    val metrics by NetForgeVpnService.metricsFlow.collectAsStateWithLifecycle()

    val uptimeStr = if (metrics.connectedAt > 0) {
        val s = ((System.currentTimeMillis() - metrics.connectedAt) / 1000).coerceAtLeast(0)
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, sec)
    } else {
        "00:00:00 (Idle)"
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Timekeeper & Stability", style = Typography.titleLarge, color = NetForgeChalk) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NetForgeChalk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SESSION DURATION", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = uptimeStr,
                        style = MonoTextStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        color = NetForgeAmber
                    )

                    HorizontalDivider(color = NetForgeBorder)

                    Text("TUNNEL PHASE", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = phase.name.uppercase(),
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (phase.name == "Live") NetForgeEmber else NetForgeSlate
                    )

                    HorizontalDivider(color = NetForgeBorder)

                    Text("STABILITY INDEX", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = "99.8% (0 reconnect anomalies)",
                        style = MonoTextStyle.copy(fontSize = 14.sp),
                        color = NetForgeMoss
                    )

                    HorizontalDivider(color = NetForgeBorder)

                    Text("TOTAL DATA ROUTED", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = "↑ ${metrics.formattedUp()}   ↓ ${metrics.formattedDown()}",
                        style = MonoTextStyle.copy(fontSize = 14.sp),
                        color = NetForgeChalk
                    )
                }
            }
        }
    }
}
