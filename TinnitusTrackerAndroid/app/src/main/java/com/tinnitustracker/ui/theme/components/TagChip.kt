package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Multi-select tag chip — toggles between outlined (unselected) and
 * filled-soft (selected). Use anywhere the user picks N from M options:
 * diary mood tags, filter chips, future onboarding category pickers.
 *
 * Note: rectangular (8 dp radius), not pill-shaped — that's the difference
 * from [Pill]. Pill = inline status badge or single-CTA; TagChip = grid
 * toggle.
 */
@Composable
fun TagChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.chip)
    val bg = if (selected) TealSoft else Surface
    val fg = if (selected) Teal else Ink2
    val border = if (selected) BorderStroke(1.dp, Teal) else BorderStroke(1.dp, Line)
    Box(
        modifier = modifier
            .pressableClickable(onClick = onToggle)
            .height(32.dp)
            .clip(shape)
            .background(bg)
            .border(border, shape)
            .padding(horizontal = Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun TagChipPreview() {
    TinnitusTrackerTheme {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            TagChip(label = "안도", selected = true, onToggle = {})
            TagChip(label = "악화", selected = false, onToggle = {})
            TagChip(label = "수면", selected = true, onToggle = {})
            TagChip(label = "휴식", selected = false, onToggle = {})
        }
    }
}
