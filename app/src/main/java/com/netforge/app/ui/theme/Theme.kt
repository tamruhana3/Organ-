package com.netforge.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DuskColorScheme = darkColorScheme(
    primary = FlexGreenPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = FlexGreenDark,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = FlexGreenBadge,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = FlexScreenBackgroundDark,
    onBackground = FlexTextLight,
    surface = FlexCardBackgroundDark,
    onSurface = FlexTextLight,
    surfaceVariant = FlexBorderDark,
    onSurfaceVariant = NetForgeSlate,
    outline = FlexBorderDark,
    error = NetForgeRust,
    onError = androidx.compose.ui.graphics.Color.White
)

private val DawnColorScheme = lightColorScheme(
    primary = FlexGreenPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = FlexGreenLight,
    onPrimaryContainer = FlexGreenDark,
    secondary = FlexGreenBadge,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = DawnBackground,
    onBackground = DawnText,
    surface = DawnPaper,
    onSurface = DawnText,
    surfaceVariant = DawnPaper2,
    onSurfaceVariant = DawnMuted,
    outline = DawnBorder,
    error = NetForgeRust,
    onError = androidx.compose.ui.graphics.Color.White
)

@Composable
fun NetForgeTheme(
    isDusk: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDusk) DuskColorScheme else DawnColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDusk
                controller.isAppearanceLightNavigationBars = !isDusk
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
