package com.tinnitustracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary           = Teal,
    onPrimary         = Surface,
    primaryContainer  = TealSoft,
    onPrimaryContainer = Teal,
    secondary         = Coral,
    onSecondary       = Surface,
    secondaryContainer = CoralSoft,
    onSecondaryContainer = Coral,
    tertiary          = Teal2,
    background        = Bg,
    onBackground      = Ink,
    surface           = Surface,
    onSurface         = Ink,
    surfaceVariant    = Bg,
    onSurfaceVariant  = Ink2,
    outline           = Line,
    outlineVariant    = Line,
    error             = Warn,
    onError           = Surface,
    errorContainer    = WarnSoft,
    onErrorContainer  = Warn,
)

@Composable
fun TinnitusTrackerTheme(
    content: @Composable () -> Unit
) {
    // System-bar appearance is managed at the Activity / per-screen level
    // (MainActivity sets the cream-default; OnboardingScreen overrides
    // to dark while its hero is visible).
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
