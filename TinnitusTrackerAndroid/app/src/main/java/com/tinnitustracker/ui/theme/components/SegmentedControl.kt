package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Surface
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme
import com.tinnitustracker.ui.theme.pressableClickable

/**
 * iOS-style segmented control. Per wireframe `.seg`:
 *   - Outer track: `Bg` background, 1 dp Line border, 12 dp radius, 4 dp padding.
 *   - 4 dp gap between segments.
 *   - Each segment: 8 dp inner radius, transparent unselected / Surface +
 *     subtle 1 dp spot shadow selected.
 *
 * Use for 2–4 mutually-exclusive options where a [Pill] row would lose its
 * sense of "this is one toggle". Currently shipped in SoundSettingsScreen
 * (`노치` / `증폭`).
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(Radius.button)
    val innerShape = RoundedCornerShape(Radius.chip)
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(outerShape)
            .background(Bg)
            .padding(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            Segment(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                innerShape = innerShape
            )
        }
    }
}

@Composable
private fun RowScope.Segment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    innerShape: androidx.compose.ui.graphics.Shape
) {
    Box(
        modifier = Modifier
            .pressableClickable(onClick = onClick)
            .weight(1f)
            .fillMaxHeight()
            .shadow(
                elevation = if (selected) 1.dp else 0.dp,
                shape = innerShape,
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(innerShape)
            .background(if (selected) Surface else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Ink else Ink2,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun SegmentedControlPreview() {
    TinnitusTrackerTheme {
        var idx by remember { mutableStateOf(0) }
        SegmentedControl(
            options = listOf("노치", "증폭"),
            selectedIndex = idx,
            onSelect = { idx = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        )
    }
}
