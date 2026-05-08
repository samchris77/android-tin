package com.tinnitustracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
