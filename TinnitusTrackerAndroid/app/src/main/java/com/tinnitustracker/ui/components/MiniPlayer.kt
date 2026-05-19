package com.tinnitustracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.tinnitustracker.ui.theme.DarkBg
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.pressableClickable
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    visible: Boolean,
    presetName: String?,
    isPlaying: Boolean,
    liveSession: AudioRepository.LiveSession?,
    sleepTimerRemainingSec: Int?,
    onTogglePlay: () -> Unit,
    onLogSession: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkBg)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = presetName ?: "소리 선택",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = elapsedLabel(liveSession),
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                        if (sleepTimerRemainingSec != null && sleepTimerRemainingSec > 0) {
                            Text(
                                text = " · 수면 ${formatMmSs(sleepTimerRemainingSec.toLong())}",
                                color = Teal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 세션 기록 button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .pressableClickable(onClick = onLogSession),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = "세션 기록",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isPlaying) Teal else Color.White.copy(alpha = 0.1f))
                        .pressableClickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "일시정지" else "재생",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun elapsedLabel(liveSession: AudioRepository.LiveSession?): String {
    if (liveSession == null) return "사운드 테라피"
    // 1 Hz tick. Paused → freeze the counter at the latest non-paused tick.
    var nowMs by remember(liveSession.startedAtEpochMs) {
        mutableStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(liveSession.startedAtEpochMs, liveSession.isPaused) {
        while (!liveSession.isPaused) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedSec = ((nowMs - liveSession.startedAtEpochMs) / 1000L).coerceAtLeast(0L)
    val prefix = if (liveSession.isPaused) "일시정지" else "재생 중"
    return "$prefix · ${formatMmSs(elapsedSec)}"
}

private fun formatMmSs(totalSec: Long): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}
