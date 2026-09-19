package com.netforge.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netforge.app.domain.model.TunnelPhase
import com.netforge.app.ui.theme.*
import com.netforge.app.ui.util.pressScale

@Composable
fun ActionPill(
    phase: TunnelPhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (phase) {
            TunnelPhase.Ready, TunnelPhase.Halted -> NetForgeAccent
            TunnelPhase.Opening -> NetForgeAccent.copy(alpha = 0.5f)
            TunnelPhase.Live -> NetForgeRust
            TunnelPhase.Error -> NetForgeEmber
        },
        label = "pillColor"
    )

    val labelText = when (phase) {
        TunnelPhase.Ready, TunnelPhase.Halted -> "Begin routing"
        TunnelPhase.Opening -> "Opening tunnel..."
        TunnelPhase.Live -> "End session"
        TunnelPhase.Error -> "Retry connection"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(backgroundColor)
            .pressScale { onClick() }
    ) {
        Text(
            text = labelText,
            style = Typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color.White
        )
    }
}
