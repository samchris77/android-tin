package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

/**
 * Standardized section header. Bold 17 sp title on the left, optional
 * trailing slot (typically a [Pill] badge like "최근 N점" or a
 * [SecondaryButton]-style ghost link). Use above any grouped section to
 * give it a consistent eyebrow — replaces the dozens of inline
 * `Text(text = "최근 기록", fontSize = ..., fontWeight = ...)` calls.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        trailing?.invoke()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun SectionHeaderPreview() {
    TinnitusTrackerTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            SectionHeader(title = "최근 기록")
            SectionHeader(
                title = "TFI 추이",
                trailing = { Pill(text = "최근 66점", variant = PillVariant.TealSoft, size = PillSize.Small) }
            )
        }
    }
}
