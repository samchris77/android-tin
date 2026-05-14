package com.tinnitustracker.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Surface as SurfaceColor
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailBottomSheet(
    detail: DayDetail,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                formatDayHeader(detail.date),
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (detail.isEmpty) "기록된 활동이 없습니다"
                else "청취 ${formatDurationKoFull(detail.totalListenMs)} · " +
                        "${detail.sessions.size}회 세션 · 일지 ${detail.diaries.size}개",
                color = Muted,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(20.dp))

            if (detail.isEmpty) {
                EmptyDetailCard()
            } else {
                if (detail.sessions.isNotEmpty()) {
                    SectionLabel("청취 세션")
                    Spacer(Modifier.height(8.dp))
                    SessionsCard(detail.sessions)
                    Spacer(Modifier.height(16.dp))
                }
                if (detail.diaries.isNotEmpty()) {
                    SectionLabel("일지")
                    Spacer(Modifier.height(8.dp))
                    DiariesCard(detail.diaries)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Ink,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun EmptyDetailCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("이 날에는 기록이 없어요", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun SessionsCard(sessions: List<RecentEntry.Session>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
    ) {
        sessions.forEachIndexed { i, s ->
            if (i > 0) RowDivider()
            SessionRow(s)
        }
    }
}

@Composable
private fun SessionRow(s: RecentEntry.Session) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(filledGraphicEq = true)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatTimeRange(s.timestampEpochMs, s.timestampEpochMs + s.durationMs),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            val dur = formatDurationKoFull(s.durationMs)
            val sub = if (s.presetLabel.isNullOrBlank()) dur else "$dur · ${s.presetLabel}"
            Text(sub, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DiariesCard(diaries: List<RecentEntry.Diary>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
    ) {
        diaries.forEachIndexed { i, d ->
            if (i > 0) RowDivider()
            DiaryRow(d)
        }
    }
}

@Composable
private fun DiaryRow(d: RecentEntry.Diary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(filledGraphicEq = false)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatClock(d.timestampEpochMs),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            val head = "이명 ${d.severity} · 스트레스 ${d.stressLevel}"
            val sub = if (d.note.isBlank()) head
            else "$head · ${d.note.split(",").joinToString(" · ")}"
            Text(sub, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun IconBadge(filledGraphicEq: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TealSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (filledGraphicEq) Icons.Filled.GraphicEq else Icons.Filled.EditNote,
            contentDescription = null,
            tint = Teal,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Line)
    )
}

private fun formatDayHeader(date: LocalDate): String {
    val dow = arrayOf("월", "화", "수", "목", "금", "토", "일")[date.dayOfWeek.value - 1]
    return "${date.monthValue}월 ${date.dayOfMonth}일 ($dow)"
}

private fun formatClock(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val dt = Instant.ofEpochMilli(epochMs).atZone(zone)
    val ampm: String
    val h12: Int
    if (dt.hour < 12) {
        ampm = "오전"; h12 = if (dt.hour == 0) 12 else dt.hour
    } else {
        ampm = "오후"; h12 = if (dt.hour == 12) 12 else dt.hour - 12
    }
    val mm = dt.minute.toString().padStart(2, '0')
    return "$ampm $h12:$mm"
}

private fun formatTimeRange(startMs: Long, endMs: Long): String =
    "${formatClock(startMs)} – ${formatClock(endMs)}"

private fun formatDurationKoFull(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return when {
        h > 0L -> "${h}시간 ${m}분"
        m > 0L -> "${m}분 ${s}초"
        else   -> "${s}초"
    }
}
