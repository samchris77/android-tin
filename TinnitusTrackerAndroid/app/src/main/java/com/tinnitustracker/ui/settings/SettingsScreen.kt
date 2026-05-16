package com.tinnitustracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.pressableClickable

/**
 * 설정 — Settings root. Re-skinned to the wireframes' design system: section
 * eyebrows, light card surfaces, teal "열기" affordances. Only the tutorial
 * replay row is wired today; the rest are placeholders for the planned
 * preferences (TFI cadence, daily goal, theme, etc.).
 */
@Composable
fun SettingsScreen(
    onReplayOnboarding: () -> Unit,
    onOpenTfi: () -> Unit,
    tfiCadenceWeeks: Int,
    onToggleTfiCadence: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("설정", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(Spacing.sm))
        Text("앱과 학습 자료를 조정합니다.", color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(Spacing.xxl))
        SectionLabel("학습 자료")
        Spacer(Modifier.height(8.dp))
        ActionRow(label = "튜토리얼 다시 보기", cta = "열기", onClick = onReplayOnboarding)
        Spacer(Modifier.height(8.dp))
        ActionRow(label = "이명 메커니즘 다시 읽기", cta = "준비 중", onClick = null)

        Spacer(Modifier.height(24.dp))
        SectionLabel("청취 목표")
        Spacer(Modifier.height(8.dp))
        ValueRow(label = "일일 청취 목표", value = "120 분", enabled = false)
        Spacer(Modifier.height(8.dp))
        TfiCadenceRow(weeks = tfiCadenceWeeks, onToggle = onToggleTfiCadence)
        Spacer(Modifier.height(8.dp))
        ActionRow(label = "TFI 다시 작성", cta = "작성하기", onClick = onOpenTfi)

        Spacer(Modifier.height(24.dp))
        SectionLabel("주파수")
        Spacer(Modifier.height(8.dp))
        ActionRow(label = "주파수 다시 측정", cta = "준비 중", onClick = null)
        Spacer(Modifier.height(8.dp))
        ValueRow(label = "현재 모드", value = "노치 · 4,250 Hz", enabled = false)

        Spacer(Modifier.height(24.dp))
        SectionLabel("정보")
        Spacer(Modifier.height(8.dp))
        ValueRow(label = "앱 버전", value = "1.0.0", enabled = false)
        Spacer(Modifier.height(8.dp))
        ValueRow(label = "샘플레이트", value = "44.1 kHz", enabled = false)
        Spacer(Modifier.height(8.dp))
        ValueRow(label = "출력", value = "Mono", enabled = false)
        Spacer(Modifier.height(8.dp))
        ActionRow(label = "임상 면책 조항", cta = "준비 중", onClick = null)

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
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
private fun ActionRow(label: String, cta: String, onClick: (() -> Unit)?) {
    val clickable = onClick != null
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.card),
        modifier = Modifier
            .let { if (clickable) it.pressableClickable { onClick!!() } else it }
            .fillMaxWidth()
            .shadow(if (clickable) 1.5.dp else 0.dp, RoundedCornerShape(Radius.card))
            .border(1.dp, Line, RoundedCornerShape(Radius.card))
            .semantics { contentDescription = label }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = if (clickable) Ink else Ink2,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                cta,
                color = if (clickable) Teal else Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TfiCadenceRow(weeks: Int, onToggle: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.card),
        modifier = Modifier
            .pressableClickable(onClick = onToggle)
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(Radius.card))
            .border(1.dp, Line, RoundedCornerShape(Radius.card))
            .semantics { contentDescription = "TFI 주기 ${weeks}주마다" }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TFI 주기", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${weeks}주마다", color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ValueRow(label: String, value: String, enabled: Boolean) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.card),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(Radius.card))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = if (enabled) Ink else Ink2, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(value, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
