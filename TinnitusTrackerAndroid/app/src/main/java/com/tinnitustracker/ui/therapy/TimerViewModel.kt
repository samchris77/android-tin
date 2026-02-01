package com.tinnitustracker.ui.therapy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.audio.AudioEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TimerViewModel(
    private val audioEngine: AudioEngine
) : ViewModel() {

    var remainingSeconds by mutableLongStateOf(0L)
        private set
    
    var isTimerRunning by mutableStateOf(false)
        private set

    private var timerJob: Job? = null
    private var initialVolume = 0.5f

    fun startTimer(minutes: Int) {
        stopTimer()
        val totalSeconds = minutes * 60L
        remainingSeconds = totalSeconds
        isTimerRunning = true
        
        // Save current volume to restore or fade relative to it?
        // For simplicity, we assume we fade from current.
        // But we won't read current volume from AudioEngine as it doesn't expose getter yet for UI.
        // We'll just fade setVolume.

        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                
                if (remainingSeconds <= 5) {
                    // Fade out
                    val fraction = remainingSeconds / 5.0f
                    // We need a stable reference volume. 
                    // Let's assume max volume enabled is 1.0 or whatever user set.
                    // This is tricky without knowing current volume.
                    // Ideally AudioEngine handles fadeout.
                    
                    // Simple approach: Linear fade to 0
                    // Note: This might jump if current volume was low.
                    audioEngine.setVolume(fraction * 0.5f) // Assuming 0.5f base
                }
            }
            audioEngine.stop()
            audioEngine.setVolume(0.5f) // Restore volume
            isTimerRunning = false
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        isTimerRunning = false
        remainingSeconds = 0
    }

    fun getFormattedTime(): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.SECONDS.toMinutes(remainingSeconds),
            TimeUnit.SECONDS.toSeconds(remainingSeconds) % 60
        )
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}

class TimerViewModelFactory(private val audioEngine: AudioEngine) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(audioEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
