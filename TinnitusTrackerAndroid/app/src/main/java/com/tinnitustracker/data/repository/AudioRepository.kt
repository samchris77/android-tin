package com.tinnitustracker.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tinnitustracker.audio.engine.AudioEngine
import com.tinnitustracker.audio.service.TherapyAudioService

/**
 * Single point of access for audio playback. Forwards the engine's flows
 * directly (no flatMap / null handling — engine is a process-singleton in
 * `TinnitusTrackerApp`) and wraps playback control so starting playback
 * also promotes [TherapyAudioService] to foreground (and stopping tears
 * the service down).
 */
class AudioRepository(
    private val appContext: Context,
    private val engine: AudioEngine
) {

    // ── Engine flows — same StateFlow instances the ViewModel used to read ──
    val frequency     = engine.frequency
    val volume        = engine.volume
    val mode          = engine.mode
    val isPlayingFlow = engine.isPlayingFlow

    // ── Engine setters (1-to-1 forwarding) ─────────────────────────────────
    fun setFrequency(hz: Float)      = engine.setFrequency(hz)
    fun setVolume(v: Float)          = engine.setVolume(v)
    fun setMode(m: AudioEngine.Mode) = engine.setMode(m)

    // ── Playback control (also drives foreground service) ─────────────────
    fun togglePlay() {
        if (engine.isPlayingFlow.value) stop() else start()
    }

    fun start() {
        if (engine.isPlayingFlow.value) return
        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, TherapyAudioService::class.java)
                .setAction(TherapyAudioService.ACTION_START)
        )
        engine.start()
    }

    fun stop() {
        engine.stop()
        appContext.stopService(Intent(appContext, TherapyAudioService::class.java))
    }
}
