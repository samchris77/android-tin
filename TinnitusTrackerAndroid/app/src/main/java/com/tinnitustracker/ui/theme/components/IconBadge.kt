package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.CoralSoft
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

/**
 * Square tinted-tile-with-icon. Standard leading affordance for list rows:
 * session / diary rows in 기록, settings shortcut rows, day-detail entries.
 *
 * Defaults to 40 dp + 8 dp radius + TealSoft tile with a Teal icon. The
 * [variant] enum lets a row express semantic difference (e.g. a diary row
 * uses [IconBadgeVariant.Coral] to differentiate from a session row's
 * [IconBadgeVariant.Teal]) without rebuilding the box.
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: IconBadgeVariant = IconBadgeVariant.Teal,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(Radius.chip))
            .background(variant.bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = variant.fg,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

enum class IconBadgeVariant(val bg: Color, val fg: Color) {
    Teal (TealSoft, com.tinnitustracker.ui.theme.Teal),
    Coral(CoralSoft, com.tinnitustracker.ui.theme.Coral)
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun IconBadgePreview() {
    TinnitusTrackerTheme {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            IconBadge(icon = Icons.Filled.GraphicEq, contentDescription = null)
            IconBadge(icon = Icons.Filled.EditNote, contentDescription = null, variant = IconBadgeVariant.Coral)
            IconBadge(icon = Icons.Filled.GraphicEq, contentDescription = null, size = 48.dp)
        }
    }
}
