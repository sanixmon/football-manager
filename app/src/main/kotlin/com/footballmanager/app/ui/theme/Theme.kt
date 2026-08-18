package com.footballmanager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FmDarkColorScheme = darkColorScheme(
    primary = FmAccentBlue,
    onPrimary = FmTextHighContrast,
    secondary = FmAccentCyan,
    onSecondary = FmDarkBg,
    tertiary = FmContinueGreen,
    onTertiary = FmDarkBg,
    background = FmDarkBg,
    onBackground = FmTextPrimary,
    surface = FmSurface,
    onSurface = FmTextPrimary,
    surfaceVariant = FmSurfaceAlt,
    onSurfaceVariant = FmTextSecondary,
    outline = FmBorder,
    outlineVariant = FmBorder,
)

@Composable
fun FootballManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FmDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
