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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.tinnitustracker.ui.theme.Elevation
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
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
    val dayDetail by vm.dayDetail.collectAsStateWithLifecycle()
    val dailyGoalMin by vm.dailyGoalMin.collectAsStateWithLifecycle()
    val dailyGoalMs = dailyGoalMin * 60_000L

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var showQuickLog by remember { mutableStateOf(false) }
    var selectedEntryForOptions by remember { mutableStateOf<RecentEntry?>(null) }

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
                        dailyGoalMs = dailyGoalMs,
                        onPrev = vm::previousMonth,
                        onNext = vm::nextMonth,
                        onDayTap = vm::selectDay,
                        onEntryOptions = { selectedEntryForOptions = it }
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

        if (selectedEntryForOptions != null) {
            EntryOptionsBottomSheet(
                entry = selectedEntryForOptions!!,
                onDismiss = { selectedEntryForOptions = null },
                onDelete = {
                    when (it) {
                        is RecentEntry.Session -> vm.deleteSession(it.id)
                        is RecentEntry.Diary -> vm.deleteDiary(it.id)
                    }
                    selectedEntryForOptions = null
                }
            )
        }

        val detail = dayDetail
        if (detail != null) {
            DayDetailBottomSheet(
                detail = detail,
                onDismiss = { vm.selectDay(null) }
            )
        }
    }
}

@Composable
private fun QuickLogFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .pressableClickable(onClick = onClick)
            .shadow(Elevation.hero, CircleShape, spotColor = Teal.copy(alpha = 0.6f))
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
    dailyGoalMs: Long,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDayTap: (java.time.LocalDate) -> Unit,
    onEntryOptions: (RecentEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            CalendarCard(state = state, dailyGoalMs = dailyGoalMs, onPrev = onPrev, onNext = onNext, onDayTap = onDayTap)
        }
        item { Spacer(Modifier.height(16.dp)) }
        // 주간 요약 hidden 2026-05-19 — re-enable when design is reworked.
        // item { WeeklySummarySection(state.weeklySummaries) }
        // item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader("최근 기록")
        }
        item { Spacer(Modifier.height(8.dp)) }
        if (entries.isEmpty()) {
            item { EmptyCard("아직 기록된 활동이 없습니다") }
        } else {
            item { RecentEntriesCard(entries, onEntryOptions) }
        }
    }
}

@Composable
private fun WeeklySummarySection(weeks: List<WeekSummary>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("주간 요약")
        Spacer(Modifier.height(Spacing.sm))
        weeks.forEachIndexed { i, w ->
            if (i > 0) Spacer(Modifier.height(Spacing.md))
            if (w.isEmpty) WeeklySummaryEmptyCard(weekLabel = formatWeekLabel(w))
            else WeeklySummaryCard(week = w)
        }
    }
}

@Composable
private fun WeeklySummaryCard(week: WeekSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(Radius.card))
            .clip(RoundedCornerShape(Radius.card))
            .background(SurfaceColor)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatWeekLabel(week),
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(TealSoft)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            ) {
                Text(
                    "${week.daysWithData}일",
                    color = Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(Spacing.md))
        MetricRow(label = "이명 평균", value = formatScoreValue(week.tinnitusAvg))
        Spacer(Modifier.height(Spacing.sm))
        MetricRow(label = "스트레스 평균", value = formatScoreValue(week.stressAvg))
        Spacer(Modifier.height(Spacing.sm))
        MetricRow(label = "청취 시간", value = formatListenValue(week.listenMs))
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Ink2, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WeeklySummaryEmptyCard(weekLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(Radius.card))
            .clip(RoundedCornerShape(Radius.card))
            .background(SurfaceColor)
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            weekLabel,
            color = Ink2,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(Spacing.xs))
        Text("데이터 없음", color = Muted, fontSize = 12.sp)
    }
}

private fun formatWeekLabel(week: WeekSummary): String {
    val s = week.startDate
    val e = week.endDate
    return if (s.month == e.month) {
        "${s.monthValue}.${"%02d".format(s.dayOfMonth)} – ${"%02d".format(e.dayOfMonth)}"
    } else {
        "${s.monthValue}.${"%02d".format(s.dayOfMonth)} – ${e.monthValue}.${"%02d".format(e.dayOfMonth)}"
    }
}

private fun formatScoreValue(avg: Float?): String =
    if (avg == null) "—" else "%.1f / 10".format(avg)

private fun formatListenValue(ms: Long): String {
    if (ms == 0L) return "0분"
    val totalMin = ms / 60_000L
    val h = totalMin / 60L
    val m = totalMin % 60L
    return if (h > 0L) "${h}시간 ${m}분" else "${m}분"
}

@Composable
private fun RecentEntriesCard(entries: List<RecentEntry>, onEntryOptions: (RecentEntry) -> Unit) {
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
            RecentEntryRow(entry, onClickOptions = { onEntryOptions(entry) })
        }
    }
}

@Composable
private fun RecentEntryRow(entry: RecentEntry, onClickOptions: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
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
                modifier = Modifier.size(Spacing.xl)
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
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            "···",
            color = Muted,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .pressableClickable(onClick = onClickOptions)
                .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
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
        item { TfiTrendCard(assessments) }
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
private fun TfiTrendCard(assessments: List<TFIAssessment>) {
    // observeAll() returns newest-first. The sparkline draws oldest → newest left-to-right.
    val chronological = remember(assessments) { assessments.asReversed() }
    val latest = assessments.firstOrNull()
    val first = chronological.firstOrNull()
    val delta = if (latest != null && first != null && latest.id != first.id)
        latest.totalScore - first.totalScore else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TFI 추이", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            if (latest != null) {
                Text(
                    "최근 ${latest.totalScore}점",
                    color = Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            trendSubtitle(chronological.size, delta),
            color = Muted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(Spacing.lg))
        if (chronological.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("아직 표시할 추이가 없습니다", color = Muted, fontSize = 12.sp)
            }
        } else {
            TfiSparkline(
                scores = chronological.map { it.totalScore },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // MCID legend marker
            Box(
                modifier = Modifier
                    .size(width = Spacing.md, height = Spacing.md)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TealSoft)
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                "MCID 13점 — 임상적으로 유의미한 변화",
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

private fun trendSubtitle(n: Int, delta: Int?): String {
    if (n == 0) return "검사를 시작하면 추이가 표시됩니다"
    if (n == 1 || delta == null) return "$n 회 기록"
    val arrow = when {
        delta <= -13 -> "↓ ${-delta}점 (호전)"
        delta >= 13  -> "↑ ${delta}점 (악화)"
        delta < 0    -> "↓ ${-delta}점"
        delta > 0    -> "↑ ${delta}점"
        else         -> "변화 없음"
    }
    return "$n 회 기록 · 첫 검사 대비 $arrow"
}

@Composable
private fun TfiSparkline(scores: List<Int>, modifier: Modifier = Modifier) {
    val latestPointColor = Teal
    val lineColor = Teal
    val mcidBandColor = TealSoft
    val gridColor = Line
    val mutedDot = Muted
    val mcidWorseColor = Color(0xFFE07A5F)   // coral-ish — worsening jump
    val mcidBetterColor = Color(0xFF2A9D8F)  // green-teal — improving jump

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padL = 28f
        val padR = 8f
        val padT = 8f
        val padB = 18f
        val plotW = (w - padL - padR).coerceAtLeast(1f)
        val plotH = (h - padT - padB).coerceAtLeast(1f)

        fun xAt(i: Int): Float =
            if (scores.size <= 1) padL + plotW / 2f
            else padL + plotW * i / (scores.size - 1).toFloat()

        fun yAt(score: Int): Float {
            val clamped = score.coerceIn(0, 100)
            return padT + plotH * (1f - clamped / 100f)
        }

        // Grid lines + Y labels at 0/50/100
        listOf(0, 50, 100).forEach { tick ->
            val y = yAt(tick)
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(padL, y),
                end   = androidx.compose.ui.geometry.Offset(w - padR, y),
                strokeWidth = 1f
            )
        }

        // MCID band around latest score (±13)
        val latest = scores.last()
        val bandTop = yAt((latest + 13).coerceAtMost(100))
        val bandBot = yAt((latest - 13).coerceAtLeast(0))
        drawRect(
            color = mcidBandColor.copy(alpha = 0.35f),
            topLeft = androidx.compose.ui.geometry.Offset(padL, bandTop),
            size = androidx.compose.ui.geometry.Size(plotW, (bandBot - bandTop).coerceAtLeast(1f))
        )

        // Polyline
        if (scores.size >= 2) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(xAt(0), yAt(scores[0]))
                for (i in 1 until scores.size) lineTo(xAt(i), yAt(scores[i]))
            }
            drawPath(
                path = path,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }

        // Points + MCID-significant deltas
        scores.forEachIndexed { i, s ->
            val center = androidx.compose.ui.geometry.Offset(xAt(i), yAt(s))
            val isLatest = i == scores.lastIndex
            val deltaFromPrev = if (i == 0) 0 else s - scores[i - 1]
            val markerColor = when {
                isLatest -> latestPointColor
                deltaFromPrev >= 13 -> mcidWorseColor
                deltaFromPrev <= -13 -> mcidBetterColor
                else -> mutedDot
            }
            // Outer halo on MCID-significant or latest
            val isSignificant = isLatest || kotlin.math.abs(deltaFromPrev) >= 13
            if (isSignificant) {
                drawCircle(
                    color = markerColor.copy(alpha = 0.18f),
                    radius = 9f,
                    center = center
                )
            }
            drawCircle(color = markerColor, radius = if (isLatest) 5f else 3.5f, center = center)
            if (isLatest) {
                drawCircle(color = Color.White, radius = 2f, center = center)
            }
        }
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
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
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
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
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
            .padding(vertical = Spacing.md),
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
    dailyGoalMs: Long,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDayTap: (java.time.LocalDate) -> Unit
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
        Spacer(Modifier.height(Spacing.sm))
        DayGrid(state.cells, dailyGoalMs, onDayTap)
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
                .size(Spacing.section),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "이전 달",
                tint = Teal,
                modifier = Modifier.size(Spacing.xl)
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
                .size(Spacing.section),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "다음 달",
                tint = Teal,
                modifier = Modifier.size(Spacing.xl)
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
private fun DayGrid(cells: List<DayCell>, dailyGoalMs: Long, onDayTap: (java.time.LocalDate) -> Unit) {
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
                    DayCellView(
                        cell = cell,
                        dailyGoalMs = dailyGoalMs,
                        modifier = Modifier.weight(1f),
                        onClick = { onDayTap(cell.date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, dailyGoalMs: Long, modifier: Modifier, onClick: () -> Unit) {
    val bg = when {
        !cell.inCurrentMonth -> Color.Transparent
        else -> heatColor(cell.totalMs, dailyGoalMs)
    }
    val textColor = if (cell.inCurrentMonth) {
        // High-intensity cells need light text for legibility.
        if (cell.totalMs > 0 && dailyGoalMs > 0 && cell.totalMs > dailyGoalMs * 0.5) Color.White
        else Ink
    } else {
        Muted
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .pressableClickable(onClick = onClick)
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
        Spacer(Modifier.width(Spacing.sm))
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

private fun heatColor(ms: Long, dailyGoalMs: Long): Color {
    if (ms <= 0L || dailyGoalMs <= 0L) return Heat0
    val ratio = (ms.toDouble() / dailyGoalMs.toDouble()).coerceAtMost(1.0)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryOptionsBottomSheet(
    entry: RecentEntry,
    onDismiss: () -> Unit,
    onDelete: (RecentEntry) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            Text(
                text = "기록 옵션",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressableClickable { onDelete(entry) }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("삭제", color = Color(0xFFC44536), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
