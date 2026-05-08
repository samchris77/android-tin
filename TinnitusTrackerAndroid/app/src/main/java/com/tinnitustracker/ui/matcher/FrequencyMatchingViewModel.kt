package com.tinnitustracker.ui.matcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tinnitustracker.audio.engine.AudioEngine
import com.tinnitustracker.data.repository.UserSettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

class FrequencyMatchingViewModel(
    private val engine: AudioEngine,
    private val repo: UserSettingsRepository
) : ViewModel() {

    val frequency  = engine.frequency
    val volume     = engine.volume
    val mode       = engine.mode
    val isPlaying  = engine.isPlayingFlow

    // ── Octave confusion check ────────────────────────────────────────────
    private val _showOctaveCheck = MutableStateFlow(false)
    val showOctaveCheck = _showOctaveCheck.asStateFlow()

    private var candidateHz = 1000f     // locked when the check fires
    private var octaveCheckJob: Job? = null

    /** Call every time the user moves the dial; resets the 2-second timer. */
    fun onDialInteraction() {
        octaveCheckJob?.cancel()
        _showOctaveCheck.value = false
        octaveCheckJob = viewModelScope.launch {
            delay(2_000L)
            candidateHz = engine.frequency.value
            _showOctaveCheck.value = true
        }
    }

    /** Play an octave variant without permanently changing the matched frequency.
     *  direction: -1 = one octave down, 0 = original, +1 = one octave up */
    fun playOctave(direction: Int) {
        val hz = when (direction) {
            -1   -> (candidateHz / 2f).coerceIn(MIN_HZ, MAX_HZ)
            1    -> (candidateHz * 2f).coerceIn(MIN_HZ, MAX_HZ)
            else -> candidateHz
        }
        engine.setFrequency(hz)
        if (!engine.isPlayingFlow.value) engine.start()
    }

    /** Save the currently playing frequency to DataStore and dismiss the card. */
    fun confirmPitch() {
        viewModelScope.launch {
            repo.saveMatchedFrequency(engine.frequency.value)
            repo.setHasTonalTinnitus(true)
            _showOctaveCheck.value = false
        }
    }

    /** User cannot find a clear tonal pitch; mark as non-tonal and dismiss. */
    fun skipTonalPitch() {
        viewModelScope.launch { repo.setHasTonalTinnitus(false) }
    }

    // ── Volume (safe, hard-capped at 70 %) ───────────────────────────────
    fun setVolumeSafe(v: Float) = engine.setVolume(v.coerceAtMost(0.70f))

    // ── Existing methods ─────────────────────────────────────────────────
    fun frequencyToSlider(hz: Float): Float {
        val lo = log10(MIN_HZ.toDouble())
        val hi = log10(MAX_HZ.toDouble())
        return ((log10(hz.toDouble()) - lo) / (hi - lo)).toFloat().coerceIn(0f, 1f)
    }

    fun setSliderFrequency(t: Float) {
        val lo = log10(MIN_HZ.toDouble())
        val hi = log10(MAX_HZ.toDouble())
        val hz = 10.0.pow(lo + t.coerceIn(0f, 1f) * (hi - lo)).toFloat()
        engine.setFrequency((hz / 10f).roundToInt() * 10f)
    }

    fun setVolume(v: Float) = engine.setVolume(v)

    fun setMode(m: AudioEngine.Mode) = engine.setMode(m)

    fun stepFrequency(delta: Int) {
        val hz = (engine.frequency.value + delta).coerceIn(MIN_HZ, MAX_HZ)
        engine.setFrequency((hz / 10f).roundToInt() * 10f)
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
    private val engine: AudioEngine,
    private val repo: UserSettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FrequencyMatchingViewModel::class.java))
        return FrequencyMatchingViewModel(engine, repo) as T
    }
}
