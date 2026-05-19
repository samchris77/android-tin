package com.tinnitustracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.database.entities.SoundPreset
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.ListeningSessionRepository
import com.tinnitustracker.data.repository.SoundPresetRepository
import com.tinnitustracker.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class HomeUiState(
    val activePreset: SoundPreset? = null,
    val todayListenMs: Long = 0L,
    val dailyGoalMin: Int = 120,
    val treatmentWeek: Int = 1
)

class HomeViewModel(
    private val audioRepo: AudioRepository,
    private val presetRepo: SoundPresetRepository,
    private val sessionRepo: ListeningSessionRepository,
    private val settingsRepo: UserSettingsRepository
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)

    private val todayStartMs = today.atStartOfDay(zone).toInstant().toEpochMilli()
    private val todayEndMs = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    private val activePreset = settingsRepo.activePresetId
        .flatMapLatest { id -> presetRepo.observeById(id) }

    private val todaySessions = sessionRepo.observeRange(todayStartMs, todayEndMs)

    val sleepTimer: StateFlow<AudioRepository.SleepTimerState?> = audioRepo.sleepTimer

    val state: StateFlow<HomeUiState> = combine(
        activePreset,
        todaySessions,
        settingsRepo.dailyListeningGoalMin,
        settingsRepo.treatmentStartDate
    ) { preset, sessions, goalMin, startDateMs ->
        val listenMs = sessions.sumOf { it.durationMs }

        val startLocalDate = java.time.Instant.ofEpochMilli(startDateMs).atZone(zone).toLocalDate()
        val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startLocalDate, today).toInt()
        val week = (daysSinceStart / 7) + 1

        HomeUiState(
            activePreset = preset,
            todayListenMs = listenMs,
            dailyGoalMin = goalMin,
            treatmentWeek = week.coerceAtLeast(1)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    val isPlaying: StateFlow<Boolean> = audioRepo.isPlayingFlow

    fun togglePlay() {
        audioRepo.togglePlay()
    }

    fun setSleepTimer(minutes: Int) {
        audioRepo.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        audioRepo.cancelSleepTimer()
    }
}

class HomeViewModelFactory(
    private val audioRepo: AudioRepository,
    private val presetRepo: SoundPresetRepository,
    private val sessionRepo: ListeningSessionRepository,
    private val settingsRepo: UserSettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(audioRepo, presetRepo, sessionRepo, settingsRepo) as T
    }
}
