package com.tinnitustracker.audio.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.tanh
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Generates two kinds of sound centered on the user's matched tinnitus frequency:
 *
 *   MASK  — narrow band-pass noise at the matched frequency. Used for pitch matching
 *           and for in-the-moment masking of the perceived tone.
 *
 *   NOTCH — broadband white noise with a notch carved out at the matched frequency.
 *           Used for notched-sound therapy (drives lateral inhibition around the tone).
 */
class AudioEngine(@Suppress("UNUSED_PARAMETER") context: Context) {

    enum class Mode { MASK, NOTCH }

    private val tag = "AudioEngine"
    private val sampleRate = 44_100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    ) * 4

    private val filter = BiquadFilter(sampleRate)
    private var audioTrack: AudioTrack? = null
    private var processingThread: Thread? = null
    @Volatile private var isPlaying = false

    // Smoothed parameters so frequency / volume changes don't click
    private var targetFrequency = 6_000f
    private var currentFrequency = targetFrequency
    private var currentVolume = 0.5f
    private val smoothing = 0.1f

    private val _frequency = MutableStateFlow(targetFrequency)
    val frequency: StateFlow<Float> = _frequency.asStateFlow()

    private val _volume = MutableStateFlow(currentVolume)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _mode = MutableStateFlow(Mode.MASK)
    val mode: StateFlow<Mode> = _mode.asStateFlow()

    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    init { createAudioTrack() }

    private fun createAudioTrack() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun start() {
        if (isPlaying) return
        isPlaying = true
        _isPlayingFlow.value = true
        try { audioTrack?.play() } catch (e: Exception) { Log.e(tag, "play failed", e) }
        processingThread = Thread { generate() }.apply { start() }
    }

    fun stop() {
        if (!isPlaying) return
        isPlaying = false
        _isPlayingFlow.value = false
        try { processingThread?.join(500) } catch (_: InterruptedException) {}
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) { Log.e(tag, "stop failed", e) }
    }

    fun release() {
        stop()
        audioTrack?.release()
        audioTrack = null
    }

    fun setFrequency(hz: Float) {
        targetFrequency = hz.coerceIn(100f, 16_000f)
        _frequency.value = targetFrequency
    }

    fun setVolume(v: Float) {
        currentVolume = v.coerceIn(0f, 1f)
        _volume.value = currentVolume
    }

    fun setMode(m: Mode) {
        if (_mode.value != m) {
            _mode.value = m
            filter.reset() // avoid click when topology changes
        }
    }

    private fun generate() {
        val buffer = FloatArray(1024)
        filter.reset()

        // MASK uses high Q for a tonal, perceptually-matching noise.
        // NOTCH uses low Q (~3 octaves) so the cut is clearly audible.
        val maskGain = 5.0f
        val notchGain = 1.6f

        while (isPlaying) {
            currentFrequency += (targetFrequency - currentFrequency) * smoothing

            when (_mode.value) {
                Mode.MASK  -> filter.setBandPass(currentFrequency, q = 10f)
                Mode.NOTCH -> filter.setNotch(currentFrequency, q = 0.4f)
            }

            for (i in buffer.indices) {
                val noise = Random.nextFloat() * 2f - 1f
                var s = filter.process(noise)
                s *= currentVolume * if (_mode.value == Mode.MASK) maskGain else notchGain
                buffer[i] = tanh(s.toDouble()).toFloat()
            }

            val track = audioTrack ?: break
            if (isPlaying) track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
        }
    }
}
