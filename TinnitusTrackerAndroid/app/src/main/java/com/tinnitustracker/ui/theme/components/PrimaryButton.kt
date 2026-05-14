package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Surface
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme
import com.tinnitustracker.ui.theme.pressableClickable

/**
 * Primary call-to-action button. Teal background, white label, 44 dp tall,
 * 12 dp radius. Use [PrimaryButton] for the most important action on a
 * screen (one per screen — see [SecondaryButton] for everything else).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 44.dp
) {
    val shape = RoundedCornerShape(Radius.button)
    val active = enabled && !loading
    Box(
        modifier = modifier
            .pressableClickable(enabled = active, onClick = onClick)
            .height(height)
            .shadow(
                elevation = if (active) 4.dp else 0.dp,
                shape = shape,
                spotColor = Teal.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(if (active) Teal else TealSoft)
            .padding(horizontal = Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Surface,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(
                text = text,
                color = if (active) Surface else Muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun PrimaryButtonPreview() {
    TinnitusTrackerTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            PrimaryButton(text = "저장", onClick = {}, modifier = Modifier.fillMaxWidth(0.33f))
            PrimaryButton(text = "비활성", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(0.5f))
            PrimaryButton(text = "...", onClick = {}, loading = true, modifier = Modifier.fillMaxWidth())
        }
    }
}
