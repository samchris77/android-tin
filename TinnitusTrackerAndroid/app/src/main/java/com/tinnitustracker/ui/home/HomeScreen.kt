package com.tinnitustracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.OrangeAccent

/**
 * Stub for the daily landing screen. The final design (per
 * `wiki/app/navigation.md`) carries a listening-progress ring, active-preset
 * play button, sleep-timer chip and a conditional TFI prompt. For now this
 * exists to anchor the 4-tab shell and route users to the working playback
 * surface in 소리.
 */
@Composable
fun HomeScreen(onStartTherapy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("홈", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp,
            modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(64.dp))

        Text(
            "준비 중",
            color = Ink2,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "곧 일일 청취 진도와 빠른 재생이 추가됩니다.",
            color = Muted,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OrangeAccent)
                .clickable(onClick = onStartTherapy)
                .semantics { contentDescription = "사운드 치료 시작하기" }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "사운드 치료 시작하기",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "소리 탭에서 주파수 매칭을 진행하세요.",
            color = Muted,
            fontSize = 12.sp
        )
    }
}
