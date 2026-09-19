package com.netforge.app.ui.bench

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netforge.app.service.NetForgeVpnService
import com.netforge.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowmeterScreen(
    onNavigateBack: () -> Unit
) {
    val metrics by NetForgeVpnService.metricsFlow.collectAsStateWithLifecycle()
    val speedHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(Unit) {
        while (true) {
            val speedKb = (metrics.speedDownBps / 1024f).coerceAtLeast(0f)
            if (speedHistory.size >= 25) {
                speedHistory.removeAt(0)
            }
            speedHistory.add(speedKb)
            delay(500)
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Flowmeter Throughput", style = Typography.titleLarge, color = NetForgeChalk) },
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
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (speedHistory.size > 1) {
                        val maxSpeed = (speedHistory.maxOrNull() ?: 100f).coerceAtLeast(50f)
                        val stepX = size.width / (speedHistory.size - 1)

                        for (i in 0 until speedHistory.size - 1) {
                            val y1 = size.height - (speedHistory[i] / maxSpeed * size.height)
                            val y2 = size.height - (speedHistory[i + 1] / maxSpeed * size.height)
                            drawLine(
                                color = NetForgeMoss,
                                start = Offset(i * stepX, y1),
                                end = Offset((i + 1) * stepX, y2),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("INSTANTANEOUS DOWNLOAD RATE", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = metrics.formattedSpeedDown(),
                        style = MonoTextStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = NetForgeMoss
                    )

                    HorizontalDivider(color = NetForgeBorder)

                    Text("INSTANTANEOUS UPLOAD RATE", style = Typography.labelSmall, color = NetForgeSlate)
                    Text(
                        text = metrics.formattedSpeedUp(),
                        style = MonoTextStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = NetForgeAccent
                    )
                }
            }
        }
    }
}
