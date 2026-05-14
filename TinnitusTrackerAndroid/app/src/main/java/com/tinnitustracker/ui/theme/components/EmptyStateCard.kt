package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

/**
 * Centered "nothing here yet" card. Pass an optional [icon], required
 * [text], optional [subtext] hinting at what would put something here, and
 * an optional [cta] slot for a CTA button (typically a [SecondaryButton]).
 *
 * Wraps content in an [AppCard] so the empty state shares the same shape
 * language as filled cards. Use everywhere a list / card / sparkline has
 * zero data — defining the empty state is required per
 * `wiki/app/ux-principles.md §4`.
 */
@Composable
fun EmptyStateCard(
    text: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    icon: ImageVector? = null,
    cta: (@Composable () -> Unit)? = null,
    padding: PaddingValues = PaddingValues(vertical = Spacing.xxl, horizontal = Spacing.lg)
) {
    AppCard(modifier = modifier, padding = padding) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = text,
                color = Ink2,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            subtext?.let {
                Text(text = it, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
            cta?.invoke()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun EmptyStateCardPreview() {
    TinnitusTrackerTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            EmptyStateCard(
                text = "아직 기록된 활동이 없습니다",
                subtext = "사운드 세션을 시작하거나 일지를 추가해 보세요"
            )
            EmptyStateCard(
                text = "이 날에는 기록이 없어요",
                icon = Icons.Filled.Inbox
            )
        }
    }
}
