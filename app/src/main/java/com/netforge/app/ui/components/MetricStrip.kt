package com.netforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.model.Metrics
import com.netforge.app.ui.theme.*

@Composable
fun MetricStrip(
    metrics: Metrics,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricChip(
            label = "UPLOAD",
            value = metrics.formattedUp(),
            subtext = metrics.formattedSpeedUp()
        )
        MetricChip(
            label = "DOWNLOAD",
            value = metrics.formattedDown(),
            subtext = metrics.formattedSpeedDown()
        )
        MetricChip(
            label = "PING",
            value = "${metrics.pingMs} ms",
            subtext = "rtt"
        )
        MetricChip(
            label = "JITTER",
            value = String.format(java.util.Locale.US, "%.1f ms", metrics.jitterMs),
            subtext = "variance"
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    subtext: String
) {
    Box(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NetForgePaper)
            .border(1.dp, NetForgeBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = label,
                style = Typography.labelSmall.copy(fontSize = 10.sp),
                color = NetForgeSlate
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MonoTextStyle.copy(fontSize = 14.sp),
                color = NetForgeChalk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = Typography.bodySmall.copy(fontSize = 11.sp),
                color = NetForgeSlate
            )
        }
    }
}
