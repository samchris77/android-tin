package com.tinnitustracker.ui.records

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinnitustracker.data.database.entities.TFIAssessment
import com.tinnitustracker.ui.assessment.TfiScoring
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Heat0
import com.tinnitustracker.ui.theme.Heat1
import com.tinnitustracker.ui.theme.Heat2
import com.tinnitustracker.ui.theme.Heat3
import com.tinnitustracker.ui.theme.Heat4
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Surface as SurfaceColor
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordsScreen(vm: RecordsViewModel, onStartTfi: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val recentEntries by vm.recentEntries.collectAsStateWithLifecycle()
    val tfiAssessments by vm.tfiAssessments.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var showQuickLog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(20.dp))
            Text(
                "기록",
                color = Ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            TabBar(
                selectedIndex = pagerState.currentPage,
                onSelect = { idx -> scope.launch { pagerState.animateScrollToPage(idx) } }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OverviewHistoryTab(
                        state = state,
                        entries = recentEntries,
                        onPrev = vm::previousMonth,
                        onNext = vm::nextMonth
                    )
                    1 -> TfiScoresTab(
                        assessments = tfiAssessments,
                        onStartTfi = onStartTfi
                    )
                }
            }
        }

        QuickLogFab(
            onClick = { showQuickLog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        )

        if (showQuickLog) {
            QuickLogBottomSheet(
                onDismiss = { showQuickLog = false },
                onSave = { severity, stress, tags ->
                    vm.addDiaryEntry(severity, stress, tags)
                    showQuickLog = false
                }
            )
        }
    }
}

@Composable
private fun QuickLogFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .pressableClickable(onClick = onClick)
            .shadow(6.dp, CircleShape, spotColor = Teal.copy(alpha = 0.6f))
            .clip(CircleShape)
            .background(Teal)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "기록 추가",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ── Tab bar ──────────────────────────────────────────────────────────────────

@Composable
private fun TabBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("개요 및 기록", "TFI 점수")
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Bg,
        contentColor = Teal,
        divider = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Line)
            )
        }
    ) {
        labels.forEachIndexed { i, label ->
            Tab(
                selected = selectedIndex == i,
                onClick = { onSelect(i) },
                selectedContentColor = Teal,
                unselectedContentColor = Muted,
                text = {
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = if (selectedIndex == i) FontWeight.SemiBold
                                     else FontWeight.Normal
                    )
                }
            )
        }
    }
}

// ── Tab 1: Overview & History ────────────────────────────────────────────────

@Composable
private fun OverviewHistoryTab(
    state: RecordsUiState,
    entries: List<RecentEntry>,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            CalendarCard(state = state, onPrev = onPrev, onNext = onNext)
        }
        item { Spacer(Modifier.height(16.dp)) }
        item { WeeklySummaryPlaceholder() }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader("최근 기록")
        }
        item { Spacer(Modifier.height(8.dp)) }
        if (entries.isEmpty()) {
            item { EmptyCard("아직 기록된 활동이 없습니다") }
        } else {
            item { RecentEntriesCard(entries) }
        }
    }
}

@Composable
private fun WeeklySummaryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        Text(
            "주간 요약",
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "준비 중",
            color = Muted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun RecentEntriesCard(entries: List<RecentEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
    ) {
        entries.forEachIndexed { i, entry ->
            if (i > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Line)
                )
            }
            RecentEntryRow(entry)
        }
    }
}

@Composable
private fun RecentEntryRow(entry: RecentEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TealSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (entry) {
                    is RecentEntry.Session -> Icons.Filled.GraphicEq
                    is RecentEntry.Diary   -> Icons.Filled.EditNote
                },
                contentDescription = null,
                tint = Teal,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatEntryDateLine(entry.timestampEpochMs),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitleFor(entry),
                color = Muted,
                fontSize = 12.sp
            )
        }
        Text(
            "···",
            color = Muted,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, end = 4.dp)
        )
    }
}

private fun subtitleFor(entry: RecentEntry): String = when (entry) {
    is RecentEntry.Session -> {
        val dur = formatDurationKo(entry.durationMs) + " 청취"
        if (entry.presetLabel.isNullOrBlank()) dur
        else "$dur · ${entry.presetLabel}"
    }
    is RecentEntry.Diary -> {
        val head = "이명 ${entry.severity} · 스트레스 ${entry.stressLevel}"
        if (entry.note.isBlank()) head
        else "$head · ${entry.note.split(",").joinToString(" · ")}"
    }
}

// ── Tab 2: TFI Scores ────────────────────────────────────────────────────────

@Composable
private fun TfiScoresTab(
    assessments: List<TFIAssessment>,
    onStartTfi: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item { TfiTrendPlaceholder(assessments) }
        item { Spacer(Modifier.height(20.dp)) }
        item { SectionHeader("이전 검사") }
        item { Spacer(Modifier.height(8.dp)) }
        if (assessments.isEmpty()) {
            item { EmptyCard("이전 검사 기록이 없습니다") }
        } else {
            item { TfiHistoryCard(assessments) }
        }
        item { Spacer(Modifier.height(20.dp)) }
        item { StartTfiCta(onStartTfi) }
    }
}

@Composable
private fun TfiTrendPlaceholder(assessments: List<TFIAssessment>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "TFI 추이",
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            val latest = assessments.firstOrNull()
            if (latest != null) {
                Text(
                    "최근 ${latest.totalScore}점",
                    color = Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "준비 중 — 추이 차트 (MCID 13점)",
            color = Muted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun TfiHistoryCard(assessments: List<TFIAssessment>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
    ) {
        assessments.forEachIndexed { i, a ->
            if (i > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Line)
                )
            }
            TfiHistoryRow(a)
        }
    }
}

@Composable
private fun TfiHistoryRow(a: TFIAssessment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatEntryDateLine(a.takenAtEpochMs),
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${a.totalScore} / 100",
                color = Muted,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(TealSoft)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                TfiScoring.severityKorean(a.totalScore),
                color = Teal,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StartTfiCta(onStartTfi: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressableClickable(onClick = onStartTfi)
            .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Teal.copy(alpha = 0.45f))
            .clip(RoundedCornerShape(12.dp))
            .background(Teal)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "새 검사 시작",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Calendar (unchanged from prior implementation) ───────────────────────────

@Composable
private fun CalendarCard(
    state: RecordsUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        CalendarHeader(state, onPrev, onNext)
        Spacer(Modifier.height(12.dp))
        WeekdayRow()
        Spacer(Modifier.height(6.dp))
        DayGrid(state.cells, state.maxDayMs)
        Spacer(Modifier.height(12.dp))
        LegendRow(state.monthTotalMs)
    }
}

@Composable
private fun CalendarHeader(
    state: RecordsUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "기록 달력",
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .pressableClickable(onClick = onPrev)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "이전 달",
                tint = Ink2,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            state.monthYear.format(fmt),
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(
            modifier = Modifier
                .pressableClickable(onClick = onNext)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "다음 달",
                tint = Ink2,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WeekdayRow() {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEach { label ->
            Text(
                label,
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayGrid(cells: List<DayCell>, maxDayMs: Long) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cell = cells[row * 7 + col]
                    DayCellView(cell, maxDayMs, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, maxDayMs: Long, modifier: Modifier) {
    val bg = when {
        !cell.inCurrentMonth -> Heat0
        else -> heatColor(cell.totalMs, maxDayMs)
    }
    val textColor = if (cell.inCurrentMonth) {
        // High-intensity cells need light text for legibility.
        if (cell.totalMs > 0 && maxDayMs > 0 && cell.totalMs > maxDayMs * 0.5) Color.White
        else Ink
    } else {
        Muted
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(
                if (cell.isToday) Modifier.border(
                    width = 2.dp,
                    color = Teal,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            cell.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (cell.isToday) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun LegendRow(monthTotalMs: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("적음", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.width(6.dp))
        listOf(Heat1, Heat2, Heat3, Heat4).forEach { c ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(c)
            )
            Spacer(Modifier.width(3.dp))
        }
        Spacer(Modifier.width(3.dp))
        Text("많음", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text("이번 달: ", color = Muted, fontSize = 12.sp)
        Text(
            formatDuration(monthTotalMs),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Shared bits ──────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
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
private fun EmptyCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Muted, fontSize = 12.sp)
    }
}

private fun heatColor(ms: Long, maxMs: Long): Color {
    if (ms <= 0L || maxMs <= 0L) return Heat0
    val ratio = ms.toDouble() / maxMs.toDouble()
    return when {
        ratio <= 0.25 -> Heat1
        ratio <= 0.50 -> Heat2
        ratio <= 0.75 -> Heat3
        else          -> Heat4
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalMin = ms / 60_000L
    val h = totalMin / 60L
    val m = totalMin % 60L
    return when {
        h > 0L -> "${h}h ${m}m"
        else   -> "${m}m"
    }
}

private fun formatDurationKo(ms: Long): String {
    val totalMin = (ms / 60_000L).coerceAtLeast(0L)
    val h = totalMin / 60L
    val m = totalMin % 60L
    return when {
        h > 0L -> "${h}시간 ${m}분"
        else   -> "${m}분"
    }
}

private fun formatEntryDateLine(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val dt = Instant.ofEpochMilli(epochMs).atZone(zone)
    val dow = arrayOf("월", "화", "수", "목", "금", "토", "일")[dt.dayOfWeek.value - 1]
    val ampm: String
    val h12: Int
    if (dt.hour < 12) {
        ampm = "오전"
        h12 = if (dt.hour == 0) 12 else dt.hour
    } else {
        ampm = "오후"
        h12 = if (dt.hour == 12) 12 else dt.hour - 12
    }
    val mm = dt.minute.toString().padStart(2, '0')
    return "${dt.monthValue}월 ${dt.dayOfMonth}일 ($dow) · $ampm $h12:$mm"
}
