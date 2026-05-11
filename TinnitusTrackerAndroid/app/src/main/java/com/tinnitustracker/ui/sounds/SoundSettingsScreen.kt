package com.tinnitustracker.ui.sounds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft

/**
 * Sound settings page (Direction A: Card stack). Five cards: preset switcher,
 * frequency display, processing mode, color noise picker, ambient mix. The
 * frequency card's "다시 측정" pill opens the matcher subscreen. All values
 * are currently hardcoded; each card has a TODO for live data wiring.
 */
@Composable
fun SoundSettingsScreen(onOpenMatcher: () -> Unit) {
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
                "미리듣기 ▶",
                color = Muted,
                fontSize = 11.sp,
                modifier = Modifier.clickable { /* TODO: play full-preset preview */ }
            )
        }

        // 1. Preset switcher card
        PresetSwitcherCard()
        Spacer(Modifier.height(12.dp))

        // 2. Frequency card
        FrequencyCard(onOpenMatcher = onOpenMatcher)
        Spacer(Modifier.height(12.dp))

        // 3. Processing mode card
        ProcessingModeCard()
        Spacer(Modifier.height(12.dp))

        // 4. Color noise card
        ColorNoiseCard()
        Spacer(Modifier.height(12.dp))

        // 5. Ambient mix card
        AmbientMixCard()

        Spacer(Modifier.height(96.dp))
    }
}

// ── Preset Switcher Card ────────────────────────────────────────────────────

@Composable
private fun PresetSwitcherCard() {
    Surface(
        color = TealSoft,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Teal.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow("현재 프리셋")
                Text(
                    "저녁 휴식 ▾",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .clickable { /* TODO: save preset */ }
                    .border(1.dp, Line, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "+ 저장",
                    color = Ink,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

// ── Frequency Card ──────────────────────────────────────────────────────────

@Composable
private fun FrequencyCard(onOpenMatcher: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow("이명 주파수")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "4,250",
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
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.clickable(onClick = onOpenMatcher)
            ) {
                Text(
                    "다시 측정 ›",
                    color = Teal,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Processing Mode Card ────────────────────────────────────────────────────

@Composable
private fun ProcessingModeCard() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Eyebrow("처리 방식")
            Spacer(Modifier.height(10.dp))

            // Segmented control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg, RoundedCornerShape(12.dp))
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Active segment: 노치
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { /* TODO: wire to AudioRepository.setProcessingMode(NOTCH) */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "노치",
                            color = Ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Inactive segment: 증폭
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { /* TODO: wire to AudioRepository.setProcessingMode(AMPLIFY) */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "증폭",
                        color = Ink2,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "광대역 노이즈에서 이명 주파수를 차단해 측방 억제를 유도합니다 (TRT 권장).",
                color = Muted,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// ── Color Noise Card ────────────────────────────────────────────────────────

@Composable
private fun ColorNoiseCard() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Eyebrow("컬러 노이즈")
                Text(
                    "▶ 미리듣기",
                    color = Muted,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable { /* TODO: play color noise preview */ }
                )
            }
            Spacer(Modifier.height(10.dp))

            // Pill selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Active pill: 핑크
                Surface(
                    color = Teal,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .clickable { /* TODO: wire to AudioRepository.setColorNoise(PINK) */ }
                ) {
                    Text(
                        "핑크",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Inactive pills
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable { /* TODO: wire to AudioRepository.setColorNoise(WHITE) */ }
                        .border(1.dp, Line, RoundedCornerShape(999.dp))
                ) {
                    Text(
                        "화이트",
                        color = Ink2,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable { /* TODO: wire to AudioRepository.setColorNoise(BROWN) */ }
                        .border(1.dp, Line, RoundedCornerShape(999.dp))
                ) {
                    Text(
                        "브라운",
                        color = Ink2,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable { /* TODO: wire to AudioRepository.setColorNoise(OFF) */ }
                        .border(1.dp, Line, RoundedCornerShape(999.dp))
                ) {
                    Text(
                        "끄기",
                        color = Ink2,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ── Ambient Mix Card ────────────────────────────────────────────────────────

@Composable
private fun AmbientMixCard() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Eyebrow("자연음 믹스")
            Spacer(Modifier.height(12.dp))

            // Rain row + progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌧 빗소리", fontSize = 13.sp, color = Ink)
                Text("60%", fontSize = 13.sp, color = Teal, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Line, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.60f)
                        .height(4.dp)
                        .background(Teal, RoundedCornerShape(2.dp))
                )
            }

            Spacer(Modifier.height(12.dp))

            // Wave row + progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌊 파도소리", fontSize = 13.sp, color = Muted)
                Text("0%", fontSize = 13.sp, color = Muted)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Line, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0f)
                        .height(4.dp)
                        .background(Teal, RoundedCornerShape(2.dp))
                )
            }
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
