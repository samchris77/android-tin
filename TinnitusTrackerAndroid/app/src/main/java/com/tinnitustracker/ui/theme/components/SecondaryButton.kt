package com.tinnitustracker.ui.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme
import com.tinnitustracker.ui.theme.pressableClickable

/**
 * Secondary / ghost button. Transparent surface, 1 dp Line border, dark
 * label. Same 44 dp height + 12 dp radius as [PrimaryButton]. Use for
 * cancel / back / non-primary actions — typically paired with a Primary.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 44.dp
) {
    val shape = RoundedCornerShape(Radius.button)
    Box(
        modifier = modifier
            .pressableClickable(enabled = enabled, onClick = onClick)
            .height(height)
            .clip(shape)
            .border(BorderStroke(1.dp, Line), shape)
            .padding(horizontal = Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Ink else Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1EA)
@Composable
private fun SecondaryButtonPreview() {
    TinnitusTrackerTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            SecondaryButton(text = "이전", onClick = {}, modifier = Modifier.fillMaxWidth(0.5f))
            androidx.compose.foundation.layout.Spacer(Modifier.padding(Spacing.sm))
            SecondaryButton(text = "비활성", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
        }
    }
}
