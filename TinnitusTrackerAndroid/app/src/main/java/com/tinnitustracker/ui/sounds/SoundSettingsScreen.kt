package com.tinnitustracker.ui.sounds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.SoundPresetRepository
import com.tinnitustracker.data.repository.UserSettingsRepository

/**
 * Sound settings page (Direction A: Card stack). Five cards: preset switcher,
 * frequency display, processing mode, color noise picker, ambient mix. The
 * frequency card's "다시 측정" pill opens the matcher subscreen.
 */
@Composable
fun SoundSettingsScreen(
    audioRepo: AudioRepository,
    settingsRepo: UserSettingsRepository,
    presetRepo: SoundPresetRepository,
    onOpenMatcher: () -> Unit
) {
    val vm: SoundSettingsViewModel = viewModel(
        factory = SoundSettingsViewModelFactory(audioRepo, settingsRepo, presetRepo)
    )
    
    val activePreset by vm.activePreset.collectAsState()
    val frequency by audioRepo.frequency.collectAsState()
    val isPlaying by audioRepo.isPlayingFlow.collectAsState()
    
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Header row: title + preview button
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("소리 설정", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                if (isPlaying) "정지 ⏹" else "미리듣기 ▶",
                color = if (isPlaying) Teal else Muted,
                fontSize = 11.sp,
                modifier = Modifier
                    .pressableClickable { vm.togglePreview() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // 1. Preset switcher card
        PresetSwitcherCard(
            presetName = activePreset?.name ?: "저녁 휴식",
            onSaveClick = { showSaveDialog = true }
        )
        Spacer(Modifier.height(12.dp))

        // 2. Frequency + processing — merged into one block
        FrequencyAndProcessingCard(
            frequency = frequency,
            mode = activePreset?.processingMode ?: "notch",
            onOpenMatcher = onOpenMatcher,
            onModeChanged = { vm.updateProcessingMode(it) }
        )
        Spacer(Modifier.height(12.dp))

        // 3. Color noise card
        ColorNoiseCard(
            currentColor = activePreset?.colorNoise ?: "off",
            onColorChanged = { vm.updateColorNoise(it) }
        )
        Spacer(Modifier.height(12.dp))

        // 5. Ambient mix card
        AmbientMixCard(
            ambientMix = activePreset?.ambientMix ?: emptyMap(),
            onVolumeChanged = { source, volume -> vm.updateAmbientMix(source, volume) }
        )

        Spacer(Modifier.height(96.dp))
    }

    if (showSaveDialog) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("새 프리셋 저장", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("현재 설정된 소리 조합을 새로운 프리셋으로 저장합니다.", fontSize = 14.sp, color = Ink2)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("프리셋 이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            vm.saveAsNewPreset(newName)
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text("저장", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSaveDialog = false },
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Text("취소", color = Teal)
                }
            }
        )
    }
}

// ── Preset Switcher Card ────────────────────────────────────────────────────

@Composable
private fun PresetSwitcherCard(presetName: String, onSaveClick: () -> Unit) {
    Surface(
        color = TealSoft,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Teal.copy(alpha = 0.18f))
            .border(1.dp, Teal.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow("현재 프리셋")
                Text(
                    "$presetName ▾",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .pressableClickable(onClick = onSaveClick)
                    .shadow(1.dp, RoundedCornerShape(8.dp))
                    .border(1.dp, Line, RoundedCornerShape(8.dp))
            ) {
                Text(
                    "+ 저장",
                    color = Ink,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ── Frequency + Processing (merged) ─────────────────────────────────────────

@Composable
private fun FrequencyAndProcessingCard(
    frequency: Float,
    mode: String,
    onOpenMatcher: () -> Unit,
    onModeChanged: (String) -> Unit
) {
    val processingEnabled = mode != "off"

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.card),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(Radius.card))
            .border(1.dp, Line, RoundedCornerShape(Radius.card))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Frequency row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow("이명 주파수")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            String.format("%,d", frequency.toInt()),
                            color = Ink,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W300,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            " Hz · 고음",
                            color = Muted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Surface(
                    color = TealSoft,
                    shape = RoundedCornerShape(Radius.pill),
                    modifier = Modifier
                        .pressableClickable(onClick = onOpenMatcher)
                        .shadow(2.dp, RoundedCornerShape(Radius.pill), spotColor = Teal.copy(alpha = 0.35f))
                ) {
                    Text(
                        "다시 측정 ›",
                        color = Teal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = Spacing.sm)
                    )
                }
            }

            // Divider
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Line))

            // Master toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow("처리 방식")
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "주파수 처리 사용",
                        color = Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = processingEnabled,
                    onCheckedChange = { on ->
                        onModeChanged(if (on) "notch" else "off")
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Teal,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Muted
                    )
                )
            }

            // Segments (notch / amplify) — dimmed and non-interactive when off
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = Spacing.md)
                    .alpha(if (processingEnabled) 1f else 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Bg, RoundedCornerShape(Radius.button))
                        .border(1.dp, Line, RoundedCornerShape(Radius.button))
                        .padding(Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    val isNotch = mode == "notch"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .let {
                                if (processingEnabled) it.pressableClickable { onModeChanged("notch") } else it
                            }
                            .let {
                                if (isNotch) {
                                    it.shadow(2.dp, RoundedCornerShape(Radius.chip))
                                      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Radius.chip))
                                } else it
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "노치",
                            color = if (isNotch) Ink else Ink2,
                            fontSize = 12.sp,
                            fontWeight = if (isNotch) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    val isAmplify = mode == "amplify"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .let {
                                if (processingEnabled) it.pressableClickable { onModeChanged("amplify") } else it
                            }
                            .let {
                                if (isAmplify) {
                                    it.shadow(2.dp, RoundedCornerShape(Radius.chip))
                                      .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Radius.chip))
                                } else it
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "증폭",
                            color = if (isAmplify) Ink else Ink2,
                            fontSize = 12.sp,
                            fontWeight = if (isAmplify) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "광대역 노이즈에서 이명 주파수를 차단해 측방 억제를 유도합니다 (TRT 권장).",
                    color = Muted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// ── Color Noise Card ────────────────────────────────────────────────────────

@Composable
private fun ColorNoiseCard(currentColor: String, onColorChanged: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.card),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(Radius.card))
            .border(1.dp, Line, RoundedCornerShape(Radius.card))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = Spacing.md)
        ) {
            Eyebrow("컬러 노이즈")
            Spacer(Modifier.height(Spacing.md))

            // Pill selector row — tap selected pill to deselect.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                listOf("pink" to "핑크", "white" to "화이트", "brown" to "브라운").forEach { (key, label) ->
                    val selected = currentColor == key
                    ColorPill(label = label, selected = selected) {
                        onColorChanged(if (selected) "off" else key)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPill(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Surface(
            color = Teal,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .pressableClickable(onClick = onClick)
                .shadow(2.dp, RoundedCornerShape(999.dp), spotColor = Teal.copy(alpha = 0.50f))
        ) {
            Text(
                label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = Spacing.sm)
            )
        }
    } else {
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .pressableClickable(onClick = onClick)
                .border(1.dp, Line, RoundedCornerShape(999.dp))
        ) {
            Text(
                label,
                color = Ink2,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = Spacing.sm)
            )
        }
    }
}

// ── Ambient Mix Card ────────────────────────────────────────────────────────

@Composable
private fun AmbientMixCard(ambientMix: Map<String, Float>, onVolumeChanged: (String, Float) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = Spacing.md)
        ) {
            Eyebrow("자연음 믹스")
            Spacer(Modifier.height(12.dp))

            // Rain row + progress bar
            val rainVol = ambientMix["rain"] ?: 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌧 빗소리", fontSize = 13.sp, color = if (rainVol > 0f) Ink else Muted)
                Text("${(rainVol * 100).toInt()}%", fontSize = 13.sp, color = if (rainVol > 0f) Teal else Muted, fontWeight = if (rainVol > 0f) FontWeight.Bold else FontWeight.Normal)
            }
            Spacer(Modifier.height(Spacing.sm))
            Slider(
                value = rainVol,
                onValueChange = { onVolumeChanged("rain", it) },
                colors = SliderDefaults.colors(
                    thumbColor = Teal,
                    activeTrackColor = Teal,
                    inactiveTrackColor = Line
                )
            )

            Spacer(Modifier.height(12.dp))

            // Wave row + progress bar
            val wavesVol = ambientMix["beach"] ?: 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌊 파도소리", fontSize = 13.sp, color = if (wavesVol > 0f) Ink else Muted)
                Text("${(wavesVol * 100).toInt()}%", fontSize = 13.sp, color = if (wavesVol > 0f) Teal else Muted, fontWeight = if (wavesVol > 0f) FontWeight.Bold else FontWeight.Normal)
            }
            Spacer(Modifier.height(Spacing.sm))
            Slider(
                value = wavesVol,
                onValueChange = { onVolumeChanged("beach", it) },
                colors = SliderDefaults.colors(
                    thumbColor = Teal,
                    activeTrackColor = Teal,
                    inactiveTrackColor = Line
                )
            )

            Spacer(Modifier.height(12.dp))

            // Brook row + progress bar
            val brookVol = ambientMix["brook"] ?: 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌲 시냇물", fontSize = 13.sp, color = if (brookVol > 0f) Ink else Muted)
                Text("${(brookVol * 100).toInt()}%", fontSize = 13.sp, color = if (brookVol > 0f) Teal else Muted, fontWeight = if (brookVol > 0f) FontWeight.Bold else FontWeight.Normal)
            }
            Spacer(Modifier.height(Spacing.sm))
            Slider(
                value = brookVol,
                onValueChange = { onVolumeChanged("brook", it) },
                colors = SliderDefaults.colors(
                    thumbColor = Teal,
                    activeTrackColor = Teal,
                    inactiveTrackColor = Line
                )
            )

            Spacer(Modifier.height(12.dp))

            // Fireplace row + progress bar
            val fireVol = ambientMix["fireplace"] ?: 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🔥 모닥불", fontSize = 13.sp, color = if (fireVol > 0f) Ink else Muted)
                Text("${(fireVol * 100).toInt()}%", fontSize = 13.sp, color = if (fireVol > 0f) Teal else Muted, fontWeight = if (fireVol > 0f) FontWeight.Bold else FontWeight.Normal)
            }
            Spacer(Modifier.height(Spacing.sm))
            Slider(
                value = fireVol,
                onValueChange = { onVolumeChanged("fireplace", it) },
                colors = SliderDefaults.colors(
                    thumbColor = Teal,
                    activeTrackColor = Teal,
                    inactiveTrackColor = Line
                )
            )
        }
    }
}

// ── Eyebrow helper ──────────────────────────────────────────────────────────

@Composable
private fun Eyebrow(text: String) {
    Text(
        text.uppercase(),
        color = Muted,
        fontSize = 9.5.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )
}
