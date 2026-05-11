package com.tinnitustracker.ui.records

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft

/**
 * Stub for the Records tab, re-skinned to the wireframes' design system.
 * Final design carries three lenses (달력 / 청취 로그 / TFI 추이); for now
 * we render a faint week-strip preview + chip row to hint at what's coming.
 */
@Composable
fun RecordsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("기록", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "청취 적응 진도와 설문 추이를 한눈에 확인합니다.",
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        // Faded week-strip preview — visual hint at the calendar lens.
        Spacer(Modifier.height(28.dp))
        Eyebrow("미리보기")
        Spacer(Modifier.height(8.dp))
        FadedWeekPreview()

        Spacer(Modifier.height(36.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "준비 중",
                color = Ink2,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "청취 기록, 일일 목표 달성, TFI 추이가 곧 표시됩니다.",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            PreviewChip("달력")
            PreviewChip("청취 로그")
            PreviewChip("TFI 추이")
        }
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text,
        color = Coral,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun FadedWeekPreview() {
    // Seven small cells, mostly TealSoft, one Teal "today" — at low contrast
    // so it reads as preview chrome, not real data.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val days = listOf("월", "화", "수", "목", "금", "토", "일")
        days.forEachIndexed { i, label ->
            val isToday = i == 4
            Column(
                modifier = Modifier.weight(if (isToday) 1.4f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isToday) 54.dp else 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isToday -> Teal.copy(alpha = 0.85f)
                                i < 4 -> TealSoft
                                else -> MaterialTheme.colorScheme.background
                            }
                        )
                        .border(
                            width = if (isToday || i < 4) 0.dp else 1.dp,
                            color = Line,
                            shape = RoundedCornerShape(10.dp)
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    label,
                    color = if (isToday) Coral else Muted,
                    fontSize = 10.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun PreviewChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(TealSoft)
            .border(1.dp, Teal.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
