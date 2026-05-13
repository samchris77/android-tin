package com.tinnitustracker.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.database.entities.ListeningSession
import com.tinnitustracker.data.repository.ListeningSessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModel(
    private val sessionRepo: ListeningSessionRepository,
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
    private val sessionRepo: ListeningSessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecordsViewModel::class.java))
        return RecordsViewModel(sessionRepo) as T
    }
}
