package com.tinnitustracker.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.random.Random

class AudioEngine {
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    ) * 2 // Double buffer size for safety

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var processingThread: Thread? = null

    // Filter and State
    private val filter = BiquadFilter(sampleRate)
    private var currentFrequency = 440.0f
    private var currentVolume = 0.5f

    // Smooth transition (interpolation) variables
    private var targetFrequency = 440.0f
    private var targetVolume = 0.5f
    private val smoothingFactor = 0.1f // Simple exponential smoothing

    init {
        createAudioTrack()
        // Initialize filter
        updateFilter()
    }

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
        audioTrack?.play()

        processingThread = Thread {
            val buffer = FloatArray(1024)
            while (isPlaying) {
                // Interpolate parameters for smooth changes
                currentFrequency += (targetFrequency - currentFrequency) * smoothingFactor
                currentVolume += (targetVolume - currentVolume) * smoothingFactor
                
                // Update filter coefficients periodically (every block) to avoid artifacting too much, 
                // or per sample for super high quality (too expensive). 
                // Doing it once per buffer block is usually fine for UI sliders.
                updateFilter()

                // Generate and process audio
                for (i in buffer.indices) {
                    // White Gaussian Noise approx or Uniform -1..1
                    // Using uniform -1..1 for simplicity similar to iOS implementation
                    val drySample = Random.nextFloat() * 2.0f - 1.0f 
                    
                    // Apply Filter
                    val wetSample = filter.process(drySample)
                    
                    // Apply Volume (Gain)
                    // The filter has a high gain (Q factor logic in iOS had gain=10).
                    // We might need to normalize or limit. 
                    // For now, applying volume directly.
                    buffer[i] = wetSample * currentVolume
                }

                // Write to AudioTrack
                audioTrack?.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
        }.apply { start() }
    }

    fun stop() {
        isPlaying = false
        try {
            processingThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        audioTrack?.stop()
        audioTrack?.flush()
    }

    fun setFrequency(frequency: Float) {
        targetFrequency = frequency.coerceIn(20f, 16000f)
    }

    fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f)
    }

    private fun updateFilter() {
        // Matching iOS BPF parameters:
        // Bandwidth ~0.8 (Q factor derived from bandwidth is approx frequency/bandwidth in octaves logic, 
        // but iOS AVAudioUnitEQ bandwidth is in Octaves). 
        // Q = frequency / bandwidth. NO, AVAudioUnitEQ.bandwidth IS in octaves.
        // Q = 1.0 / (2.0 * sinh(ln(2.0)/2.0 * bandwidthOctaves * w0/sin(w0))) approx.
        // For standard Biquad recipes, Q is typically 1.0 for generic BP.
        // iOS: bandwidth = 0.8 octaves. 
        // Let's approximate Q ~ 1.5 to 2.0 for a focused sound.
        
        filter.setBandPass(currentFrequency, 2.0f)
    }

    fun release() {
        stop()
        audioTrack?.release()
    }
}
