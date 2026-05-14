package com.tinnitustracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable
import kotlinx.coroutines.delay

/**
 * Floating in-app indicator shown only while a listening session is open. Lives
 * above the bottom nav and animates in / out. Tap routes to the 소리 tab.
 *
 * Format:
 *  - playing → Teal pill: "GraphicEq · 추적 중 MM:SS"
 *  - paused (transient focus loss) → Muted pill: "Pause · 일시정지 MM:SS"
 */
@Composable
fun LiveSessionPill(
    session: AudioRepository.LiveSession?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = session != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        // `session` may be null during the exit transition — grab a stable snapshot.
        val snap = session ?: return@AnimatedVisibility
        PillBody(snap, onClick)
    }
}

@Composable
private fun PillBody(
    session: AudioRepository.LiveSession,
    onClick: () -> Unit
) {
    var elapsedMs by remember { mutableStateOf(System.currentTimeMillis() - session.startedAtEpochMs) }
    LaunchedEffect(session.startedAtEpochMs) {
        while (true) {
            elapsedMs = System.currentTimeMillis() - session.startedAtEpochMs
            delay(1_000)
        }
    }

    val bg = if (session.isPaused) TealSoft else Teal
    val fg = if (session.isPaused) Muted else Color.White
    val label = if (session.isPaused) "일시정지" else "추적 중"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .pressableClickable(onClick = onClick)
                .shadow(4.dp, RoundedCornerShape(100.dp), spotColor = Teal.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(100.dp))
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (session.isPaused) Icons.Filled.Pause
                              else Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "$label  ${formatMmSs(elapsedMs)}",
                color = fg,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatMmSs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mm = totalSec / 60L
    val ss = totalSec % 60L
    return "${mm.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}"
}
