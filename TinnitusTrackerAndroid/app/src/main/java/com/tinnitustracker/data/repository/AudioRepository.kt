package com.tinnitustracker.data.repository

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.tinnitustracker.audio.engine.AudioEngine
import com.tinnitustracker.audio.routing.AudioFocusController
import com.tinnitustracker.audio.service.TherapyAudioService

/**
 * Single point of access for audio playback. Forwards the engine's flows
 * directly (no flatMap / null handling — engine is a process-singleton in
 * `TinnitusTrackerApp`) and wraps playback control so starting playback
 * also requests audio focus and promotes [TherapyAudioService] to foreground.
 *
 * Audio-focus interruption handling:
 *  - `LOSS_TRANSIENT` / `LOSS_TRANSIENT_CAN_DUCK` (call, navigation prompt,
 *    another media app playing briefly): engine is silenced but the service
 *    stays in foreground and `wasPlayingBeforeInterruption` is latched true.
 *  - `GAIN` after a transient loss: engine restarts automatically.
 *  - `LOSS` (another media app took focus for the foreseeable future): full
 *    teardown — engine off, service stopped, the latch is cleared so we do
 *    *not* spuriously auto-resume later.
 */
class AudioRepository(
    private val appContext: Context,
    private val engine: AudioEngine
) {
    private val tag = "AudioRepository"

    private val focus = AudioFocusController(appContext, ::onFocusChange)

    /** True iff playback was active when an audio-focus transient loss arrived. */
    @Volatile private var wasPlayingBeforeInterruption = false

    // ── Engine flows — same StateFlow instances the ViewModel used to read ──
    val frequency     = engine.frequency
    val volume        = engine.volume
    val mode          = engine.mode
    val isPlayingFlow = engine.isPlayingFlow

    // ── Engine setters (1-to-1 forwarding) ─────────────────────────────────
    fun setFrequency(hz: Float)      = engine.setFrequency(hz)
    fun setVolume(v: Float)          = engine.setVolume(v)
    fun setMode(m: AudioEngine.Mode) = engine.setMode(m)

    // ── Playback control (also drives focus + foreground service) ─────────
    fun togglePlay() {
        if (engine.isPlayingFlow.value) stop() else start()
    }

    fun start() {
        if (engine.isPlayingFlow.value) return
        if (!focus.request()) return
        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, TherapyAudioService::class.java)
                .setAction(TherapyAudioService.ACTION_START)
        )
        engine.start()
    }

    fun stop() {
        wasPlayingBeforeInterruption = false
        engine.stop()
        focus.abandon()
        appContext.stopService(Intent(appContext, TherapyAudioService::class.java))
    }

    private fun onFocusChange(change: Int) {
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(tag, "focus LOSS → full stop")
                wasPlayingBeforeInterruption = false
                engine.stop()
                appContext.stopService(Intent(appContext, TherapyAudioService::class.java))
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (engine.isPlayingFlow.value) {
                    Log.d(tag, "focus LOSS_TRANSIENT → pause engine, keep service")
                    wasPlayingBeforeInterruption = true
                    engine.stop()
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (wasPlayingBeforeInterruption) {
                    Log.d(tag, "focus GAIN after transient loss → resume engine")
                    wasPlayingBeforeInterruption = false
                    engine.start()
                }
            }
        }
    }
}
