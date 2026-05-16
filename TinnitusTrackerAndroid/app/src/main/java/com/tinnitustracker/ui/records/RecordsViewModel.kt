package com.tinnitustracker.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.database.entities.DiaryEntry
import com.tinnitustracker.data.database.entities.ListeningSession
import com.tinnitustracker.data.database.entities.TFIAssessment
import com.tinnitustracker.data.repository.DiaryRepository
import com.tinnitustracker.data.repository.ListeningSessionRepository
import com.tinnitustracker.data.repository.TfiRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class DayCell(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val isToday: Boolean,
    val totalMs: Long
)

data class DayDetail(
    val date: LocalDate,
    val sessions: List<RecentEntry.Session>,
    val diaries: List<RecentEntry.Diary>
) {
    val isEmpty: Boolean get() = sessions.isEmpty() && diaries.isEmpty()
    val totalListenMs: Long get() = sessions.sumOf { it.durationMs }
}

data class RecordsUiState(
    val monthYear: YearMonth,
    val cells: List<DayCell>,
    val monthTotalMs: Long,
    val maxDayMs: Long,
    val weeklySummaries: List<WeekSummary>
)

/**
 * Aggregates for a single Sunday-anchored week, visible under the calendar.
 * Each metric is null/zero when no data exists for that bucket.
 */
data class WeekSummary(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val tinnitusAvg: Float?,
    val stressAvg: Float?,
    val listenMs: Long,
    val daysWithData: Int
) {
    val isEmpty: Boolean get() = tinnitusAvg == null && stressAvg == null && listenMs == 0L
}

/** One row in the Recent Entries list — either an auto-logged session or a diary note. */
sealed class RecentEntry {
    abstract val id: Long
    abstract val timestampEpochMs: Long

    data class Session(
        override val id: Long,
        override val timestampEpochMs: Long,
        val durationMs: Long,
        val presetLabel: String?
    ) : RecentEntry()

    data class Diary(
        override val id: Long,
        override val timestampEpochMs: Long,
        val severity: Int,
        val stressLevel: Int,
        val note: String
    ) : RecentEntry()
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel(
    private val sessionRepo: ListeningSessionRepository,
    private val diaryRepo: DiaryRepository,
    private val tfiRepo: TfiRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.from(today()))

    val state: StateFlow<RecordsUiState> = visibleMonth
        .flatMapLatest { ym ->
            val grid = buildGridDates(ym)
            val rangeStart = grid.first().atStartOfDay(zone).toInstant().toEpochMilli()
            // Exclusive upper bound: start of the day AFTER the last grid cell.
            val rangeEndExclusive = grid.last().plusDays(1)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            combine(
                sessionRepo.observeRange(rangeStart, rangeEndExclusive),
                diaryRepo.observeRange(rangeStart, rangeEndExclusive)
            ) { sessions, diaries -> assemble(ym, grid, sessions, diaries) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecordsUiState(
                monthYear = visibleMonth.value,
                cells = buildGridDates(visibleMonth.value).map {
                    DayCell(it, it.month == visibleMonth.value.month, it == today(), 0L)
                },
                monthTotalMs = 0L,
                maxDayMs = 0L,
                weeklySummaries = emptyList()
            )
        )

    /** Merged session + diary rows, newest first, capped at 50. */
    val recentEntries: StateFlow<List<RecentEntry>> = combine(
        sessionRepo.observeRange(0L, Long.MAX_VALUE),
        diaryRepo.observeRange(0L, Long.MAX_VALUE)
    ) { sessions, diaries ->
        val merged = ArrayList<RecentEntry>(sessions.size + diaries.size)
        sessions.forEach {
            merged += RecentEntry.Session(
                id = it.id,
                timestampEpochMs = it.startedAtEpochMs,
                durationMs = it.durationMs,
                presetLabel = it.presetLabel
            )
        }
        diaries.forEach {
            merged += RecentEntry.Diary(
                id = it.id,
                timestampEpochMs = it.date,
                severity = it.severity,
                stressLevel = it.stressLevel,
                note = it.note
            )
        }
        merged.sortedByDescending { it.timestampEpochMs }.take(50)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tfiAssessments: StateFlow<List<TFIAssessment>> = tfiRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedDay = MutableStateFlow<LocalDate?>(null)

    val dayDetail: StateFlow<DayDetail?> = selectedDay.flatMapLatest { day ->
        if (day == null) flowOf(null)
        else {
            val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            combine(
                sessionRepo.observeRange(start, end),
                diaryRepo.observeRange(start, end)
            ) { sessions, diaries ->
                DayDetail(
                    date = day,
                    sessions = sessions
                        .map {
                            RecentEntry.Session(
                                id = it.id,
                                timestampEpochMs = it.startedAtEpochMs,
                                durationMs = it.durationMs,
                                presetLabel = it.presetLabel
                            )
                        }
                        .sortedByDescending { it.timestampEpochMs },
                    diaries = diaries
                        .map {
                            RecentEntry.Diary(
                                id = it.id,
                                timestampEpochMs = it.date,
                                severity = it.severity,
                                stressLevel = it.stressLevel,
                                note = it.note
                            )
                        }
                        .sortedByDescending { it.timestampEpochMs }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectDay(date: LocalDate?) {
        selectedDay.value = date
    }

    /**
     * Persists a quick-log diary entry. Tags are joined into [DiaryEntry.note]
     * with comma separators; v1 has no free-form note field in the bottom
     * sheet, so the column is effectively a packed tag list for now.
     */
    fun addDiaryEntry(severity: Int, stressLevel: Int, tags: List<String>) {
        viewModelScope.launch {
            diaryRepo.upsert(
                DiaryEntry(
                    date = System.currentTimeMillis(),
                    severity = severity.coerceIn(0, 10),
                    stressLevel = stressLevel.coerceIn(0, 10),
                    note = tags.joinToString(",")
                )
            )
        }
    }

    fun previousMonth() {
        visibleMonth.value = visibleMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        visibleMonth.value = visibleMonth.value.plusMonths(1)
    }

    /**
     * Returns the 42 LocalDates that fill a 6-row × 7-col grid for [ym],
     * starting on the Sunday on/before the 1st and ending on the Saturday
     * on/after the last day of the month.
     */
    private fun buildGridDates(ym: YearMonth): List<LocalDate> {
        val first = ym.atDay(1)
        val firstDow = first.dayOfWeek                // MONDAY..SUNDAY
        // Days from grid-start (Sunday) back to the 1st. Sunday=0, Monday=1, ..., Saturday=6.
        val leading = if (firstDow == DayOfWeek.SUNDAY) 0 else firstDow.value
        val start = first.minusDays(leading.toLong())
        return List(42) { i -> start.plusDays(i.toLong()) }
    }

    private fun assemble(
        ym: YearMonth,
        grid: List<LocalDate>,
        sessions: List<ListeningSession>,
        diaries: List<DiaryEntry>
    ): RecordsUiState {
        val today = today()
        // Bucket sessions by local date (start-of-day in `zone`).
        val sessionMsByDate = HashMap<LocalDate, Long>(sessions.size * 2)
        for (s in sessions) {
            val d = java.time.Instant.ofEpochMilli(s.startedAtEpochMs)
                .atZone(zone).toLocalDate()
            sessionMsByDate[d] = (sessionMsByDate[d] ?: 0L) + s.durationMs
        }
        // Bucket diary entries by local date.
        val diariesByDate = HashMap<LocalDate, MutableList<DiaryEntry>>(diaries.size * 2)
        for (e in diaries) {
            val d = java.time.Instant.ofEpochMilli(e.date)
                .atZone(zone).toLocalDate()
            diariesByDate.getOrPut(d) { mutableListOf() } += e
        }
        val cells = grid.map { d ->
            val inMonth = YearMonth.from(d) == ym
            DayCell(
                date = d,
                inCurrentMonth = inMonth,
                isToday = d == today,
                totalMs = sessionMsByDate[d] ?: 0L
            )
        }
        val monthTotalMs = cells.filter { it.inCurrentMonth }.sumOf { it.totalMs }
        val maxDayMs = cells.filter { it.inCurrentMonth }.maxOfOrNull { it.totalMs } ?: 0L
        val weeklySummaries = buildWeekSummaries(ym, grid, sessionMsByDate, diariesByDate)
        return RecordsUiState(
            monthYear = ym,
            cells = cells,
            monthTotalMs = monthTotalMs,
            maxDayMs = maxDayMs,
            weeklySummaries = weeklySummaries
        )
    }

    /**
     * Chunk the 42-day Sunday-anchored grid into 6 weeks; keep only weeks that
     * contain at least one day from [ym]. Aggregate listen minutes from
     * [sessionMsByDate] and tinnitus/stress means from [diariesByDate].
     */
    private fun buildWeekSummaries(
        ym: YearMonth,
        grid: List<LocalDate>,
        sessionMsByDate: Map<LocalDate, Long>,
        diariesByDate: Map<LocalDate, List<DiaryEntry>>
    ): List<WeekSummary> = grid.chunked(7)
        .filter { week -> week.any { YearMonth.from(it) == ym } }
        .map { week ->
            val weekDiaries = week.flatMap { diariesByDate[it].orEmpty() }
            val listenMs = week.sumOf { sessionMsByDate[it] ?: 0L }
            val tinnitusAvg = weekDiaries.takeIf { it.isNotEmpty() }
                ?.map { it.severity }?.average()?.toFloat()
            val stressAvg = weekDiaries.takeIf { it.isNotEmpty() }
                ?.map { it.stressLevel }?.average()?.toFloat()
            val daysWithData = week.count { d ->
                (sessionMsByDate[d] ?: 0L) > 0L || !diariesByDate[d].isNullOrEmpty()
            }
            WeekSummary(
                startDate = week.first(),
                endDate = week.last(),
                tinnitusAvg = tinnitusAvg,
                stressAvg = stressAvg,
                listenMs = listenMs,
                daysWithData = daysWithData
            )
        }
}

class RecordsViewModelFactory(
    private val sessionRepo: ListeningSessionRepository,
    private val diaryRepo: DiaryRepository,
    private val tfiRepo: TfiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecordsViewModel::class.java))
        return RecordsViewModel(sessionRepo, diaryRepo, tfiRepo) as T
    }
}
