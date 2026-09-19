package com.netforge.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object Motion {
    const val durationFast = 150
    const val durationBase = 250
    const val durationSlow = 400
    const val durationSlower = 600

    val easeStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easeEmphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    val springBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val springGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
