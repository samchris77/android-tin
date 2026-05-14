package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.CoralSoft
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Surface
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme
import com.tinnitustracker.ui.theme.pressableClickable

/**
 * Round badge / pill / chip-style indicator. Use for status badges,
 * tappable shortcuts, segmented selectors, severity labels — anywhere a
 * pill-shaped affordance is wanted.
 *
 * Variants cover the four palettes used in the wireframes: filled Teal
 * (active CTA), TealSoft (muted CTA), CoralSoft (warning/diary), Outline
 * (passive). Sizes: `Small` (24 dp tall, status badges) / `Medium` (28 dp,
 * default for selectors and inline CTAs).
 *
 * Pass [onClick] to make the pill tappable; otherwise it renders as a
 * static badge.
 */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.TealSoft,
    size: PillSize = PillSize.Medium,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(Radius.pill)
    val clickMod = onClick?.let { Modifier.pressableClickable(onClick = it) } ?: Modifier
    val borderMod = if (variant == PillVariant.Outline) {
        Modifier.border(BorderStroke(1.dp, Line), shape)
    } else Modifier
    Row(
        modifier = modifier
            .then(clickMod)
            .height(size.height)
            .clip(shape)
            .background(variant.bg)
            .then(borderMod)
            .padding(horizontal = size.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        leadingIcon?.let {
            Icon(it, contentDescription = null, tint = variant.fg, modifier = Modifier.size(size.iconSize))
        }
        Text(
            text = text,
            color = variant.fg,
            fontSize = size.fontSize,
            fontWeight = FontWeight.SemiBold
        )
        trailingIcon?.let {
            Icon(it, contentDescription = null, tint = variant.fg, modifier = Modifier.size(size.iconSize))
        }
    }
}

enum class PillVariant(val bg: Color, val fg: Color) {
    Teal     (com.tinnitustracker.ui.theme.Teal, Surface),
    TealSoft (com.tinnitustracker.ui.theme.TealSoft, com.tinnitustracker.ui.theme.Teal),
    CoralSoft(com.tinnitustracker.ui.theme.CoralSoft, com.tinnitustracker.ui.theme.Coral),
    Outline  (Color.Transparent, Ink2)
}

enum class PillSize(val height: Dp, val horizontalPadding: Dp, val fontSize: androidx.compose.ui.unit.TextUnit, val iconSize: Dp) {
    Small (24.dp, Spacing.md, 11.sp, 14.dp),
    Medium(28.dp, Spacing.md, 12.sp, 16.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun PillPreview() {
    TinnitusTrackerTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            PillVariant.values().forEach { v ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Pill(text = "$v · sm", variant = v, size = PillSize.Small)
                    Pill(text = "$v · md", variant = v, size = PillSize.Medium)
                    Pill(text = "다시 측정", variant = v, trailingIcon = Icons.Filled.ChevronRight)
                }
            }
        }
    }
}
