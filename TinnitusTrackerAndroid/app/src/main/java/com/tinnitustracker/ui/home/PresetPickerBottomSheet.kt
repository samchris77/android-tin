package com.tinnitustracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.data.database.entities.SoundPreset
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Surface
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetPickerBottomSheet(
    presets: List<SoundPreset>,
    activePresetId: Long,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 40.dp)) {
            Text(
                "프리셋 선택",
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.md))
            if (presets.isEmpty()) {
                Text(
                    "저장된 프리셋이 없습니다.",
                    color = Muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = Spacing.lg)
                )
            } else {
                presets.forEach { preset ->
                    PresetRow(
                        preset = preset,
                        selected = preset.id == activePresetId,
                        onClick = { onPick(preset.id) }
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }
            }
        }
    }
}

@Composable
private fun PresetRow(preset: SoundPreset, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressableClickable(onClick = onClick)
            .clip(RoundedCornerShape(Radius.card))
            .background(if (selected) TealSoft else Surface)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) Teal else TealSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = if (selected) Surface else Teal,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                preset.name,
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            val subtitleParts = buildList {
                when (preset.processingMode) {
                    "notch" -> add("노치")
                    "amplify" -> add("증폭")
                    "off" -> add("주파수 끔")
                }
                if (preset.colorNoise != "off") add(preset.colorNoise.replaceFirstChar { it.uppercase() })
                if (preset.ambientMix.isNotEmpty()) add("자연음")
            }
            if (subtitleParts.isNotEmpty()) {
                Text(
                    subtitleParts.joinToString(" · "),
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Teal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
