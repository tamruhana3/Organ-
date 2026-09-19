package com.netforge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.model.Metrics
import com.netforge.app.domain.model.Node
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.ui.theme.*

@Composable
fun StatusRing(
    phase: TunnelPhase,
    metrics: Metrics,
    selectedNode: Node,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringTransition")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = Motion.easeStandard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = Motion.easeStandard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Uptime formatter
    val uptimeText = remember(metrics.connectedAt, phase) {
        if (phase == TunnelPhase.Live && metrics.connectedAt > 0) {
            val seconds = ((System.currentTimeMillis() - metrics.connectedAt) / 1000).coerceAtLeast(0)
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
        } else {
            ""
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(210.dp)
            .clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 16.dp.toPx()

            when (phase) {
                TunnelPhase.Ready -> {
                    // Thin slate border with subtle glow
                    drawCircle(
                        color = NetForgeBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = NetForgeAccent.copy(alpha = 0.12f),
                        radius = radius - 8.dp.toPx()
                    )
                }
                TunnelPhase.Opening -> {
                    // Track circle
                    drawCircle(
                        color = NetForgePaper2,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Rotating gradient sweep arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                NetForgeAccent.copy(alpha = 0.1f),
                                NetForgeAmber,
                                NetForgeAccent
                            )
                        ),
                        startAngle = sweepAngle,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                TunnelPhase.Live -> {
                    // Outer pulsing glow
                    drawCircle(
                        color = NetForgeEmber.copy(alpha = glowAlpha * 0.25f),
                        radius = radius * pulseScale,
                        center = center
                    )
                    // Double ring with Ember accent
                    drawCircle(
                        color = NetForgeEmber.copy(alpha = 0.4f),
                        radius = radius + 6.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = NetForgeEmber,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                TunnelPhase.Error -> {
                    drawCircle(
                        color = NetForgeRust.copy(alpha = 0.2f),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = NetForgeRust,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                TunnelPhase.Halted -> {
                    drawCircle(
                        color = NetForgeBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Content in the ring center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (phase) {
                TunnelPhase.Ready -> {
                    Text(
                        text = selectedNode.flagEmoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "READY",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = NetForgeChalk
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${selectedNode.latencyMs} ms",
                        style = Typography.labelSmall,
                        color = NetForgeMoss
                    )
                }
                TunnelPhase.Opening -> {
                    Text(
                        text = "CONNECTING",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                        color = NetForgeAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Handshake...",
                        style = Typography.bodySmall,
                        color = NetForgeSlate
                    )
                }
                TunnelPhase.Live -> {
                    Text(
                        text = "LIVE",
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp,
                            color = NetForgeEmber
                        )
                    )
                    if (uptimeText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uptimeText,
                            style = MonoTextStyle.copy(fontSize = 12.sp, color = NetForgeChalk)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${metrics.pingMs} ms",
                        style = Typography.labelSmall,
                        color = NetForgeMoss
                    )
                }
                TunnelPhase.Error -> {
                    Text(
                        text = "ERROR",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = NetForgeRust
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap to view log",
                        style = Typography.bodySmall,
                        color = NetForgeSlate
                    )
                }
                TunnelPhase.Halted -> {
                    Text(
                        text = "HALTED",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = NetForgeSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Session closed",
                        style = Typography.bodySmall,
                        color = NetForgeSlate
                    )
                }
            }
        }
    }
}
