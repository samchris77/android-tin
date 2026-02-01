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

class TherapyViewModel(
    private val audioEngine: AudioEngine
) : ViewModel() {

    // --- Sound Selection State ---
    var selectedResourceId by mutableStateOf<Int?>(null)
        private set

    // --- Timer State ---
    var remainingSeconds by mutableLongStateOf(0L)
        private set
    
    var isTimerRunning by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    init {
        // Restore state from AudioEngine if it's already playing
        selectedResourceId = audioEngine.activeResourceId
    }
    
    fun toggleSound(resourceId: Int) {
        if (selectedResourceId == resourceId) {
            // Stop
            audioEngine.stop()
            selectedResourceId = null
        } else {
            // Play
            selectedResourceId = resourceId
            audioEngine.playSound(resourceId)
        }
    }
    
    // Check if a specific sound is active
    fun isSoundActive(resourceId: Int): Boolean {
        // Also check if engine is actually playing? 
        // For simple UI state, checking selectedResourceId match is enough if we keep them in sync.
        // But what if generator is running? Generator clears currentResourceId in AudioEngine.
        // So checking activeResourceId is safer.
        return audioEngine.activeResourceId == resourceId
    }
    
    // Refresh state (called when screen composition might need update)
    fun refreshState() {
        selectedResourceId = audioEngine.activeResourceId
    }

    // --- Timer Logic ---

    fun startTimer(minutes: Int) {
        stopTimer()
        val totalSeconds = minutes * 60L
        remainingSeconds = totalSeconds
        isTimerRunning = true
        
        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                
                if (remainingSeconds <= 5) {
                    // Fade out logic
                     val fraction = remainingSeconds / 5.0f
                     audioEngine.setVolume(fraction * 1.0f) // Assuming max volume
                }
            }
            audioEngine.stop()
            audioEngine.setVolume(1.0f) // Restore volume
            selectedResourceId = null // Update UI state
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
        // Do NOT stop audio onViewModelCleared if we want playback to persist across tabs.
        // But if the app is destroyed/MainActivity destroyed, AudioEngine.release() is called there.
        // We only stop the timer job.
        stopTimer()
        super.onCleared()
    }
}

class TherapyViewModelFactory(private val audioEngine: AudioEngine) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TherapyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TherapyViewModel(audioEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
