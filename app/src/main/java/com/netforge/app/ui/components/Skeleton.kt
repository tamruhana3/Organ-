package com.netforge.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.netforge.app.ui.theme.Motion
import com.netforge.app.ui.theme.NetForgePaper
import com.netforge.app.ui.theme.NetForgePaper2

@Composable
fun SkeletonBox(
    height: Dp,
    width: Dp = Dp.Unspecified,
    cornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = Motion.easeStandard),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    val mod = if (width != Dp.Unspecified) modifier.width(width) else modifier.fillMaxWidth()

    Box(
        modifier = mod
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(NetForgePaper2.copy(alpha = alpha))
    )
}
