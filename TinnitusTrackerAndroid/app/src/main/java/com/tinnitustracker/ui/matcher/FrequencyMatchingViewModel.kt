package com.tinnitustracker.ui.matcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tinnitustracker.audio.engine.AudioEngine
import kotlin.math.log10
import kotlin.math.pow

class FrequencyMatchingViewModel(
    private val engine: AudioEngine
) : ViewModel() {

    val frequency = engine.frequency
    val volume = engine.volume
    val mode = engine.mode
    val isPlaying = engine.isPlayingFlow

    /** Position on a log scale between MIN_HZ and MAX_HZ — 0f .. 1f for sliders. */
    fun frequencyToSlider(hz: Float): Float {
        val lo = log10(MIN_HZ.toDouble())
        val hi = log10(MAX_HZ.toDouble())
        return ((log10(hz.toDouble()) - lo) / (hi - lo)).toFloat().coerceIn(0f, 1f)
    }

    fun setSliderFrequency(t: Float) {
        val lo = log10(MIN_HZ.toDouble())
        val hi = log10(MAX_HZ.toDouble())
        val hz = 10.0.pow(lo + t.coerceIn(0f, 1f) * (hi - lo)).toFloat()
        engine.setFrequency(hz)
    }

    fun setVolume(v: Float) = engine.setVolume(v)

    fun setMode(m: AudioEngine.Mode) = engine.setMode(m)

    fun stepFrequency(delta: Int) {
        val hz = (engine.frequency.value + delta).coerceIn(MIN_HZ, MAX_HZ)
        engine.setFrequency(hz)
    }

    fun togglePlay() {
        if (isPlaying.value) engine.stop() else engine.start()
    }

    companion object {
        const val MIN_HZ = 100f
        const val MAX_HZ = 16_000f
    }
}

class FrequencyMatchingViewModelFactory(
    private val engine: AudioEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FrequencyMatchingViewModel::class.java))
        return FrequencyMatchingViewModel(engine) as T
    }
}
