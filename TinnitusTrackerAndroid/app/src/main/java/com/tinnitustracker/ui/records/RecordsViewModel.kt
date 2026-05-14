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

data class RecordsUiState(
    val monthYear: YearMonth,
    val cells: List<DayCell>,
    val monthTotalMs: Long,
    val maxDayMs: Long
)

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
            sessionRepo.observeRange(rangeStart, rangeEndExclusive)
                .map { sessions -> assemble(ym, grid, sessions) }
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
                maxDayMs = 0L
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
        sessions: List<ListeningSession>
    ): RecordsUiState {
        val today = today()
        // Bucket sessions by local date (start-of-day in `zone`).
        val byDate = HashMap<LocalDate, Long>(sessions.size * 2)
        for (s in sessions) {
            val d = java.time.Instant.ofEpochMilli(s.startedAtEpochMs)
                .atZone(zone).toLocalDate()
            byDate[d] = (byDate[d] ?: 0L) + s.durationMs
        }
        val cells = grid.map { d ->
            val inMonth = YearMonth.from(d) == ym
            DayCell(
                date = d,
                inCurrentMonth = inMonth,
                isToday = d == today,
                totalMs = byDate[d] ?: 0L
            )
        }
        val monthTotalMs = cells.filter { it.inCurrentMonth }.sumOf { it.totalMs }
        val maxDayMs = cells.filter { it.inCurrentMonth }.maxOfOrNull { it.totalMs } ?: 0L
        return RecordsUiState(
            monthYear = ym,
            cells = cells,
            monthTotalMs = monthTotalMs,
            maxDayMs = maxDayMs
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
