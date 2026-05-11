package com.tinnitustracker.audio.routing

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log

/**
 * Wraps the `AudioFocusRequest` lifecycle. The owner supplies a single
 * change callback (delivered on the main thread) and calls [request] before
 * playback starts and [abandon] when it stops. Idempotent — repeat calls in
 * the same state are no-ops.
 *
 * `setWillPauseWhenDucked(true)` opts us out of automatic ducking: instead
 * of the system attenuating our stream, we receive `LOSS_TRANSIENT_CAN_DUCK`
 * and pause the engine cleanly (a sudden volume drop on therapeutic noise
 * is more jarring than a brief pause).
 */
class AudioFocusController(
    context: Context,
    private val onFocusChange: (Int) -> Unit
) {
    private val tag = "AudioFocusController"
    private val audioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        // System has already removed us from the focus stack on LOSS; mirror
        // that here so the next request() actually re-asks instead of
        // short-circuiting on the stale `hasFocus` flag.
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> hasFocus = false
            AudioManager.AUDIOFOCUS_GAIN -> hasFocus = true
        }
        onFocusChange(change)
    }

    private val focusRequest: AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(listener)
            .build()

    @Volatile private var hasFocus = false

    fun request(): Boolean {
        if (hasFocus) return true
        val result = audioManager.requestAudioFocus(focusRequest)
        hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (!hasFocus) Log.w(tag, "audio focus request denied (result=$result)")
        return hasFocus
    }

    fun abandon() {
        if (!hasFocus) return
        audioManager.abandonAudioFocusRequest(focusRequest)
        hasFocus = false
    }
}
