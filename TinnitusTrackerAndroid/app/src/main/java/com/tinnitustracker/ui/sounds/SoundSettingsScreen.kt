package com.tinnitustracker.ui.sounds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal

/**
 * Stub for the Sound configuration root. The final design (per
 * `wiki/app/navigation.md`) wraps preset switcher, frequency summary,
 * processing mode, color noise picker, ambient mix and a saved-preset list.
 * For now: one real card → opens the matcher as a subscreen; three disabled
 * placeholder cards mirroring the planned structure so the screen doesn't
 * feel empty.
 */
@Composable
fun SoundSettingsScreen(onOpenMatcher: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            "소리",
            color = Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "이명 주파수를 찾고, 본인에게 맞는 사운드 치료를 설정하세요.",
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        ActiveCard(
            title = "주파수 매칭 및 사운드 재생",
            subtitle = "이명과 비슷한 주파수를 찾아 노치 또는 증폭 모드로 재생합니다.",
            cta = "열기",
            onClick = onOpenMatcher
        )

        Spacer(Modifier.height(20.dp))
        SectionHeader("준비 중")

        PlaceholderCard(
            title = "컬러 노이즈",
            subtitle = "핑크 · 화이트 · 브라운 노이즈"
        )
        PlaceholderCard(
            title = "자연음 믹스",
            subtitle = "빗소리, 파도, 천둥 등 여러 레이어 혼합"
        )
        PlaceholderCard(
            title = "저장된 프리셋",
            subtitle = "자주 쓰는 조합을 이름과 함께 저장하고 불러오기"
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        color = Muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.4.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp, top = 4.dp)
    )
}

@Composable
private fun ActiveCard(
    title: String,
    subtitle: String,
    cta: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = title }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Ink2, fontSize = 12.sp, lineHeight = 18.sp)
            }
            Text(cta, color = Teal, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PlaceholderCard(title: String, subtitle: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Ink2, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
            Text("준비 중", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
