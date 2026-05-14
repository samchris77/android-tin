package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.CoralSoft
import com.tinnitustracker.ui.theme.Elevation
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Surface
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.Teal2
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

/**
 * Single card primitive. All surface containers across the app pull from here
 * so corner radius (16 dp), elevation, and padding stay in lockstep.
 *
 * Use [variant] to switch palette without rebuilding the box layout. Pass a
 * different [padding] only when the content genuinely needs it (e.g. list
 * rows that want zero outer padding so their internal dividers can run
 * edge-to-edge); the default of `Spacing.lg` (16 dp) is right for most cards.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: AppCardVariant = AppCardVariant.Default,
    padding: PaddingValues = PaddingValues(Spacing.lg),
    radius: Dp = Radius.card,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .shadow(
                elevation = variant.elevation,
                shape = shape,
                spotColor = variant.spotShadow
            )
            .clip(shape)
            .then(variant.background())
            .padding(padding)
    ) {
        content()
    }
}

enum class AppCardVariant(val elevation: Dp, val spotShadow: Color) {
    Default     (Elevation.card, Ink.copy(alpha = 0.12f)),
    TealSoft    (Elevation.card, Teal.copy(alpha = 0.10f)),
    CoralSoft   (Elevation.card, Coral.copy(alpha = 0.10f)),
    DarkHero    (Elevation.hero, Color.Black.copy(alpha = 0.25f)),
    DarkNight   (Elevation.hero, Color.Black.copy(alpha = 0.35f));

    @Composable
    internal fun background(): Modifier = when (this) {
        Default   -> Modifier.background(Surface)
        TealSoft  -> Modifier.background(com.tinnitustracker.ui.theme.TealSoft)
        CoralSoft -> Modifier.background(com.tinnitustracker.ui.theme.CoralSoft)
        DarkHero  -> Modifier.background(
            Brush.linearGradient(
                colors = listOf(Teal2, Color(0xFF16302F))
            )
        )
        DarkNight -> Modifier.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFF0F1F1E), Color(0xFF1A2E2C))
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun AppCardPreview() {
    TinnitusTrackerTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            AppCardVariant.values().forEach { v ->
                AppCard(
                    modifier = Modifier
                        .padding(bottom = Spacing.md),
                    variant = v
                ) {
                    androidx.compose.material3.Text(
                        text = "AppCard · $v",
                        color = if (v == AppCardVariant.DarkHero || v == AppCardVariant.DarkNight)
                            Color.White else Ink
                    )
                }
            }
        }
    }
}
