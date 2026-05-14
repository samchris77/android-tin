package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme
import com.tinnitustracker.ui.theme.pressableClickable

/**
 * Standard tappable list row. Optional leading slot (icon, [IconBadge], or
 * any composable), required [title], optional [subtitle], optional trailing
 * value slot, and a trailing chevron that signals "this navigates" — the
 * chevron is shown by default and can be hidden via [showChevron] for rows
 * that toggle in place.
 *
 * Use [ChevronRow] for: 설정 action rows, 홈 SleepTimerRow, 기록 recent-entries
 * rows, any "tap → drill in" row in the app.
 *
 * Press feedback comes from `pressableClickable`. Material ripple is
 * intentionally not used — the snappy scale-down is the dominant cue and
 * doubling up with a ripple feels muddled on cream surfaces.
 */
@Composable
fun ChevronRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = true,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .pressableClickable(enabled = enabled, onClick = onClick)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        leading?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = title,
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(text = it, color = Muted, fontSize = 12.sp)
            }
        }
        trailing?.invoke()
        if (showChevron) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChevronRowPreview() {
    TinnitusTrackerTheme {
        Column {
            ChevronRow(
                title = "수면 타이머",
                subtitle = "30분",
                leading = { IconBadge(icon = Icons.Filled.Schedule, contentDescription = null) },
                onClick = {}
            )
            ChevronRow(
                title = "TFI 다시 작성",
                trailing = { Pill(text = "2주마다", variant = PillVariant.TealSoft, size = PillSize.Small) },
                showChevron = false,
                onClick = {}
            )
            ChevronRow(
                title = "튜토리얼 다시 보기",
                leading = { Icon(Icons.Filled.Settings, null, tint = Muted) },
                onClick = {}
            )
        }
    }
}
