package com.tinnitustracker.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Surface as SurfaceColor
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable

/** Chip labels for the quick-log sheet. Korean register matches the rest of the app. */
private val QuickLogTags = listOf(
    "안도", "악화", "수면", "스트레스", "휴식", "집중"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickLogBottomSheet(
    onDismiss: () -> Unit,
    onSave: (severity: Int, stressLevel: Int, tags: List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var severity by remember { mutableStateOf(5) }
    var stress by remember { mutableStateOf(5) }
    val selectedTags = remember { mutableStateListOf<String>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Bg,
        contentColor = Ink
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                "기록 추가",
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "지금 상태를 빠르게 기록하세요",
                color = Muted,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(20.dp))
            SliderRow(label = "이명 크기", value = severity, onChange = { severity = it })

            Spacer(Modifier.height(12.dp))
            SliderRow(label = "스트레스", value = stress, onChange = { stress = it })

            Spacer(Modifier.height(20.dp))
            Text("태그", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickLogTags.forEach { tag ->
                    val selected = selectedTags.contains(tag)
                    TagChip(
                        label = tag,
                        selected = selected,
                        onClick = {
                            if (selected) selectedTags.remove(tag)
                            else selectedTags += tag
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressableClickable(onClick = {
                        onSave(severity, stress, selectedTags.toList())
                    })
                    .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Teal.copy(alpha = 0.45f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Teal)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "저장",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text(
                value.toString(),
                color = Teal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = Teal,
                activeTrackColor = Teal,
                inactiveTrackColor = Line,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) TealSoft else SurfaceColor
    val fg = if (selected) Teal else Ink2
    Box(
        modifier = Modifier
            .pressableClickable(onClick = onClick)
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = if (selected) Teal else Line,
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = fg,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
