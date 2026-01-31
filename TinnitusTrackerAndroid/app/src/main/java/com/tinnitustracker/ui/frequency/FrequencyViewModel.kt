package com.tinnitustracker.ui.frequency

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tinnitustracker.audio.AudioEngine
import kotlin.math.log10
import kotlin.math.pow

class FrequencyViewModel(
    private val audioEngine: AudioEngine
) : ViewModel() {

    var isPlaying by mutableStateOf(false)
        private set

    var frequency by mutableStateOf(1000f)
        private set
        
    var volume by mutableStateOf(0.5f)
        private set

    // Normalized coordinates for UI (0.0 to 1.0)
    var normalizedX by mutableStateOf(0.5f)
        private set
        
    var normalizedY by mutableStateOf(0.5f)
        private set

    init {
        // Initialize position based on default values
        updateNormalizedFromValues()
    }
    
    fun togglePlayPause() {
        if (isPlaying) {
            audioEngine.stop()
        } else {
            audioEngine.start()
        }
        isPlaying = !isPlaying
    }

    fun updateFromUserDrag(x: Float, y: Float) {
        normalizedX = x.coerceIn(0f, 1f)
        normalizedY = y.coerceIn(0f, 1f)
        
        // Logarithmic frequency mapping (20Hz to 16kHz)
        // Same as iOS: minFreq = log10(20), maxFreq = log10(16000)
        val minFreqLog = log10(20.0)
        val maxFreqLog = log10(16000.0)
        
        val logFreq = minFreqLog + normalizedX * (maxFreqLog - minFreqLog)
        val newFreq = 10.0.pow(logFreq).toFloat()
        
        // Linear volume mapping (inverted Y so up is louder? No, usually up is 0 in screen coords. 
        // iOS said: y=0 -> volume=1.0 (Top is loud), y=1 -> volume=0.0 (Bottom is quiet))
        val newVol = 1.0f - normalizedY
        
        frequency = newFreq
        volume = newVol
        
        audioEngine.setFrequency(frequency)
        audioEngine.setVolume(volume)
    }
    
    private fun updateNormalizedFromValues() {
        // Reverse calculation
        val minFreqLog = log10(20.0)
        val maxFreqLog = log10(16000.0)
        val logFreq = log10(frequency.toDouble())
        
        normalizedX = ((logFreq - minFreqLog) / (maxFreqLog - minFreqLog)).toFloat()
        normalizedY = 1.0f - volume
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}

class FrequencyViewModelFactory(private val audioEngine: AudioEngine) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FrequencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FrequencyViewModel(audioEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
