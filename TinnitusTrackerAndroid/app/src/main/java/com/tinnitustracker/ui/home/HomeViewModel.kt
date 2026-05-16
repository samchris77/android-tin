package com.tinnitustracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.data.database.entities.SoundPreset
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.ListeningSessionRepository
import com.tinnitustracker.data.repository.SoundPresetRepository
import com.tinnitustracker.data.repository.UserSettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    // Calculate start and end of today in epoch ms
    private val todayStartMs = today.atStartOfDay(zone).toInstant().toEpochMilli()
    private val todayEndMs = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    private val activePreset = settingsRepo.activePresetId
        .flatMapLatest { id -> presetRepo.observeById(id) }

    // Aggregate today's sessions
    private val todaySessions = sessionRepo.observeRange(todayStartMs, todayEndMs)

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSeconds: StateFlow<Int?> = _sleepTimerRemainingSeconds

    private var timerJob: Job? = null

    val state: StateFlow<HomeUiState> = combine(
        activePreset,
        todaySessions,
        settingsRepo.dailyListeningGoalMin,
        settingsRepo.treatmentStartDate
    ) { preset, sessions, goalMin, startDateMs ->
        val listenMs = sessions.sumOf { it.durationMs }
        
        // Calculate weeks since start date
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
        timerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingSeconds.value = null
            return
        }
        
        _sleepTimerRemainingSeconds.value = minutes * 60
        timerJob = viewModelScope.launch {
            while ((_sleepTimerRemainingSeconds.value ?: 0) > 0) {
                delay(1000)
                _sleepTimerRemainingSeconds.value = _sleepTimerRemainingSeconds.value!! - 1
            }
            // Timer finished
            _sleepTimerRemainingSeconds.value = null
            audioRepo.pause()
        }
    }
    
    fun cancelSleepTimer() {
        timerJob?.cancel()
        _sleepTimerRemainingSeconds.value = null
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
