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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinglineScreen(
    onNavigateBack: () -> Unit
) {
    var targetHost by remember { mutableStateOf("1.1.1.1") }
    var targetPort by remember { mutableStateOf("443") }
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Long>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun runPingTest() {
        isRunning = true
        results = emptyList()
        scope.launch {
            val list = mutableListOf<Long>()
            val port = targetPort.toIntOrNull() ?: 443
            for (i in 1..8) {
                val rtt = withContext(Dispatchers.IO) {
                    try {
                        val s = Socket()
                        val t0 = System.currentTimeMillis()
                        s.connect(InetSocketAddress(targetHost, port), 2500)
                        val elapsed = System.currentTimeMillis() - t0
                        s.close()
                        elapsed
                    } catch (_: Exception) {
                        -1L
                    }
                }
                list.add(rtt)
                results = list.toList()
                delay(300)
            }
            isRunning = false
        }
    }

    Scaffold(
        containerColor = NetForgeInk,
        topBar = {
            TopAppBar(
                title = { Text("Pingline RTT", style = Typography.titleLarge, color = NetForgeChalk) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NetForgeChalk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NetForgeInk)
            )
        }
    ) { padding ->
        val validResults = results.filter { it > 0 }
        val minRtt = validResults.minOrNull() ?: 0
        val maxRtt = validResults.maxOrNull() ?: 0
        val avgRtt = if (validResults.isNotEmpty()) validResults.average().toLong() else 0

        var jitter = 0.0
        if (validResults.size > 1) {
            var sumDiff = 0.0
            for (i in 1 until validResults.size) {
                sumDiff += abs(validResults[i] - validResults[i - 1])
            }
            jitter = sumDiff / (validResults.size - 1)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = targetHost,
                    onValueChange = { targetHost = it },
                    label = { Text("Target Host", color = NetForgeSlate) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetForgeAccent,
                        unfocusedBorderColor = NetForgeBorder,
                        focusedContainerColor = NetForgePaper,
                        unfocusedContainerColor = NetForgePaper,
                        focusedTextColor = NetForgeChalk,
                        unfocusedTextColor = NetForgeChalk
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(2.5f)
                )

                OutlinedTextField(
                    value = targetPort,
                    onValueChange = { targetPort = it },
                    label = { Text("Port", color = NetForgeSlate) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NetForgeAccent,
                        unfocusedBorderColor = NetForgeBorder,
                        focusedContainerColor = NetForgePaper,
                        unfocusedContainerColor = NetForgePaper,
                        focusedTextColor = NetForgeChalk,
                        unfocusedTextColor = NetForgeChalk
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                )
            }

            Button(
                onClick = { runPingTest() },
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NetForgeAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRunning) "Measuring RTT..." else "Run Ping Benchmark")
            }

            // Canvas Sparkline Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NetForgePaper)
                    .border(1.dp, NetForgeBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (validResults.size > 1) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxVal = (validResults.maxOrNull() ?: 100).toFloat().coerceAtLeast(10f)
                        val stepX = size.width / (validResults.size - 1)

                        for (i in 0 until validResults.size - 1) {
                            val y1 = size.height - (validResults[i] / maxVal * size.height)
                            val y2 = size.height - (validResults[i + 1] / maxVal * size.height)
                            drawLine(
                                color = NetForgeAccent,
                                start = Offset(i * stepX, y1),
                                end = Offset((i + 1) * stepX, y2),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = NetForgeChalk,
                                radius = 4.dp.toPx(),
                                center = Offset(i * stepX, y1)
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Run benchmark to generate sparkline", style = Typography.bodySmall, color = NetForgeSlate)
                    }
                }
            }

            // Stats grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("MIN RTT", "$minRtt ms", modifier = Modifier.weight(1f))
                StatCard("AVG RTT", "$avgRtt ms", modifier = Modifier.weight(1f))
                StatCard("MAX RTT", "$maxRtt ms", modifier = Modifier.weight(1f))
                StatCard("JITTER", String.format(java.util.Locale.US, "%.1f ms", jitter), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NetForgePaper)
            .border(1.dp, NetForgeBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, style = Typography.labelSmall.copy(fontSize = 9.sp), color = NetForgeSlate)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MonoTextStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = NetForgeChalk)
        }
    }
}
