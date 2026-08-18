package com.footballmanager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = StadiumEmerald,
    onPrimary = DeepNavy,
    secondary = ElectricBlue,
    onSecondary = TextLight,
    background = DeepNavy,
    onBackground = TextLight,
    surface = SurfaceSlate,
    onSurface = TextLight,
    surfaceVariant = BorderSlate,
    onSurfaceVariant = TextMuted,
)

@Composable
fun FootballManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
