package com.tinnitustracker.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import kotlin.math.tanh
import kotlin.random.Random

class AudioEngine(private val context: Context) {
    private val TAG = "AudioEngine"
    private val SAMPLE_RATE = 44100
    // Buffer size: larger for decoding efficiency
    private val bufferSize = AudioTrack.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    ) * 4

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var processingThread: Thread? = null

    // Filter and State
    private val filter = BiquadFilter(SAMPLE_RATE)
    
    // Shared frequency for both Matcher (BandPass) and Therapy (Notch)
    private var targetFrequency = 1000.0f
    private var currentFrequency = 1000.0f
    
    // Therapy Mode State
    private var notchEnabled = false
    
    // Volume Control
    private var currentVolume = 1.0f // Default to max
    
    // Gains
    private val THERAPY_GAIN = 3.0f
    private val GENERATOR_GAIN = 5.0f
    
    private val smoothingFactor = 0.1f 

    // Current sound resource (null = Generator/Matcher Mode)
    private var currentResourceId: Int? = null

    init {
        createAudioTrack()
    }

    private fun createAudioTrack() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
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

    /* --- Playback Control --- */

    /**
     * Starts the Tone Generator (Matcher Mode).
     * Clears any selected file resource.
     */
    fun startTone() {
        if (isPlaying && currentResourceId == null) return // Already generating
        stop()
        currentResourceId = null // Switch to Generator Mode
        start()
    }

    /**
     * Plays a specific sound file (Therapy Mode).
     */
    fun playSound(resourceId: Int) {
        if (currentResourceId == resourceId && isPlaying) return
        stop()
        currentResourceId = resourceId
        start()
    }

    fun start() {
        if (isPlaying) return
        isPlaying = true
        audioTrack?.play()

        processingThread = Thread {
            if (currentResourceId != null) {
                decodeAndPlay(currentResourceId!!)
            } else {
                generateAndPlay()
            }
        }.apply { start() }
    }

    fun stop() {
        isPlaying = false
        try {
            processingThread?.join(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        stop()
        audioTrack?.release()
    }

    /* --- Generator Logic (Matcher) --- */

    private fun generateAndPlay() {
        val buffer = FloatArray(1024)
        
        // Reset filter state for clean start
        filter.reset()
        
        while (isPlaying) {
             smoothParameters()
             // Matcher Mode: ALWAYS use BandPass to isolate the frequency
             // High Q (e.g., 10.0 or higher) for a narrow tone-like noise
             filter.setBandPass(currentFrequency, 10.0f) 

             for (i in buffer.indices) {
                 // White Noise
                 val dry = Random.nextFloat() * 2.0f - 1.0f
                 
                 // Apply BandPass
                 val wet = filter.process(dry)
                 
                 // Apply Gain (Boost for visibility)
                 var sample = wet * currentVolume * GENERATOR_GAIN
                 
                 // Soft Clip
                 sample = softClip(sample)
                 
                 buffer[i] = sample
             }
             
             writeToTrack(buffer, buffer.size)
        }
    }

    /* --- Decoder Logic (Therapy) --- */

    private fun decodeAndPlay(resourceId: Int) {
        var extractor: MediaExtractor? = null
        var codec: MediaCodec? = null

        try {
            extractor = MediaExtractor()
            val afd = context.resources.openRawResourceFd(resourceId)
            extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            val format = extractor.getTrackFormat(0)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return
            
            extractor.selectTrack(0)
            
            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val info = MediaCodec.BufferInfo()
            val timeoutUs = 10000L
            var isEOS = false
            
            var floatBuffer = FloatArray(4096) 
            
            // Reset filter state
            filter.reset()

            while (isPlaying) {
                // Input
                if (!isEOS) {
                    val inputIndex = codec.dequeueInputBuffer(timeoutUs)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                // Loop
                                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                                val retrySize = extractor.readSampleData(inputBuffer, 0)
                                if (retrySize < 0) {
                                    codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                    isEOS = true
                                } else {
                                    codec.queueInputBuffer(inputIndex, 0, retrySize, extractor.sampleTime, 0)
                                    extractor.advance()
                                }
                            } else {
                                codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                // Output
                val outputIndex = codec.dequeueOutputBuffer(info, timeoutUs)
                if (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    
                    if (outputBuffer != null && info.size > 0) {
                        val numSamples = info.size / 2
                        if (floatBuffer.size < numSamples) floatBuffer = FloatArray(numSamples)

                        smoothParameters()
                        if (notchEnabled) {
                             // Therapy Notch: Q ~ 0.4 (approx 3 octaves) for wider audible effect
                             filter.setNotch(currentFrequency, 0.4f)
                        }

                        for (i in 0 until numSamples) {
                            val low = outputBuffer.get(outputIndex * 0 + info.offset + i * 2).toInt()
                            val high = outputBuffer.get(outputIndex * 0 + info.offset + i * 2 + 1).toInt()
                            val s16 = ((high and 0xFF) shl 8) or (low and 0xFF)
                            var sample = s16.toShort() / 32768.0f

                            if (notchEnabled) {
                                sample = filter.process(sample)
                            }
                            
                            // Apply Boost
                            sample *= currentVolume * THERAPY_GAIN
                            
                            // Soft Clip
                            sample = softClip(sample)
                            
                            floatBuffer[i] = sample
                        }
                        
                        writeToTrack(floatBuffer, numSamples)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in playback loop", e)
        } finally {
            try {
                codec?.stop()
                codec?.release()
                extractor?.release()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    private fun writeToTrack(buffer: FloatArray, size: Int) {
        if (isPlaying && audioTrack != null) {
            audioTrack?.write(buffer, 0, size, AudioTrack.WRITE_BLOCKING)
        }
    }
    
    private fun softClip(sample: Float): Float {
        // Tanh soft clipping
        // range -1 to 1
        return tanh(sample.toDouble()).toFloat()
    }

    // --- Control Methods ---

    fun setNotchEnabled(enabled: Boolean) {
        if (notchEnabled != enabled) {
            notchEnabled = enabled
            filter.reset() // Reset filter state on toggle to avoid click/pop
        }
    }

    // Compatible alias
    fun setFrequency(frequency: Float) {
        setNotchFrequency(frequency)
    }

    fun setNotchFrequency(frequency: Float) {
        targetFrequency = frequency.coerceIn(20f, 16000f)
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
    }
    
    private fun smoothParameters() {
         currentFrequency += (targetFrequency - currentFrequency) * smoothingFactor
    }
}
