package com.tinnitustracker.data.repository

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.tinnitustracker.audio.engine.AudioEngine
import com.tinnitustracker.audio.routing.AudioFocusController
import com.tinnitustracker.audio.service.TherapyAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
 *
 * Session logging: a session is opened on [start] and finalised on [stop] or
 * full `LOSS`. Transient losses do NOT close the session — pausing for a call
 * and resuming counts as one session for the heatmap. The repository drops
 * sessions shorter than its minimum-duration filter (see
 * [ListeningSessionRepository.MIN_DURATION_MS]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudioRepository(
    private val appContext: Context,
    private val engine: AudioEngine,
    private val sessionRepo: ListeningSessionRepository,
    private val presetRepo: SoundPresetRepository,
    private val settingsRepo: UserSettingsRepository
) {
    private val tag = "AudioRepository"

    private val focus = AudioFocusController(appContext, ::onFocusChange)

    /** True iff playback was active when an audio-focus transient loss arrived. */
    @Volatile private var wasPlayingBeforeInterruption = false

    /** Wall-clock start of the currently-open session, or null when nothing is playing. */
    @Volatile private var currentSessionStartedAtMs: Long? = null

    // Store current preset for session logging
    @Volatile private var currentPresetName: String? = null
    
    // Segment tracking
    private data class ActiveSegment(val startedAtEpochMs: Long, val presetName: String?)
    private var currentSegment: ActiveSegment? = null
    private val currentSessionSegments = mutableListOf<SessionSegmentDraft>()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        ioScope.launch {
            settingsRepo.activePresetId
                .flatMapLatest { id -> presetRepo.observeById(id) }
                .collect { preset ->
                    if (preset != null) {
                        val m = if (preset.processingMode == "amplify") AudioEngine.Mode.MASK else AudioEngine.Mode.NOTCH
                        engine.setMode(m)
                        engine.setColorNoise(preset.colorNoise)
                        engine.setColorNoiseVolume(preset.colorNoiseVolume)
                        engine.setAmbientMix(preset.ambientMix)
                        
                        synchronized(this@AudioRepository) {
                            // If the preset actually changed (e.g., name or we just want to track config changes)
                            // We can split the segment if preset config changed, but for now we just track name.
                            // The wiki says "onSoundChanged" - any config change or just name?
                            // Let's track whenever we get a new preset config.
                            val nameChanged = currentPresetName != preset.name
                            currentPresetName = preset.name
                            
                            // If we are currently playing and name changed, finalize the current segment and start a new one
                            if (engine.isPlayingFlow.value && currentSessionStartedAtMs != null && nameChanged) {
                                val now = System.currentTimeMillis()
                                currentSegment?.let { seg ->
                                    val duration = now - seg.startedAtEpochMs
                                    if (duration > 0) {
                                        currentSessionSegments.add(SessionSegmentDraft(seg.startedAtEpochMs, duration, seg.presetName))
                                    }
                                }
                                currentSegment = ActiveSegment(now, preset.name)
                            }
                        }
                    }
                }
        }
    }

    // ── Engine flows — same StateFlow instances the ViewModel used to read ──
    val frequency     = engine.frequency
    val volume        = engine.volume
    val mode          = engine.mode
    val isPlayingFlow = engine.isPlayingFlow

    /**
     * Live state of the in-flight listening session, surfaced to the UI for the
     * `LiveSessionPill`. `null` when nothing is being tracked. `isPaused = true`
     * means a transient focus loss has muted the engine but the session is
     * still open and will resume on GAIN.
     */
    data class LiveSession(val startedAtEpochMs: Long, val isPaused: Boolean)
    private val _liveSession = MutableStateFlow<LiveSession?>(null)
    val liveSession: StateFlow<LiveSession?> = _liveSession.asStateFlow()

    // ── Engine setters (1-to-1 forwarding) ─────────────────────────────────
    fun setFrequency(hz: Float)      = engine.setFrequency(hz)
    fun setVolume(v: Float)          = engine.setVolume(v)
    fun setMode(m: AudioEngine.Mode) = engine.setMode(m)

    // ── Playback control (also drives focus + foreground service) ─────────
    fun togglePlay() {
        if (engine.isPlayingFlow.value) {
            pause()
        } else {
            if (_liveSession.value != null && _liveSession.value!!.isPaused) {
                resume()
            } else {
                start()
            }
        }
    }

    fun start() {
        if (engine.isPlayingFlow.value) return
        if (!focus.request()) return
        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, TherapyAudioService::class.java)
                .setAction(TherapyAudioService.ACTION_START)
        )
        val startedAt = System.currentTimeMillis()
        
        synchronized(this) {
            currentSessionStartedAtMs = startedAt
            currentSessionSegments.clear()
            currentSegment = ActiveSegment(startedAt, currentPresetName)
        }
        
        _liveSession.value = LiveSession(startedAt, isPaused = false)
        engine.start()
    }

    fun pause() {
        if (!engine.isPlayingFlow.value) return
        engine.stop()
        _liveSession.update { it?.copy(isPaused = true) }
        appContext.startService(
            Intent(appContext, TherapyAudioService::class.java)
                .setAction(TherapyAudioService.ACTION_PAUSE)
        )
    }

    fun resume() {
        if (engine.isPlayingFlow.value) return
        if (!focus.request()) return
        _liveSession.update { it?.copy(isPaused = false) }
        appContext.startService(
            Intent(appContext, TherapyAudioService::class.java)
                .setAction(TherapyAudioService.ACTION_RESUME)
        )
        engine.start()
    }

    fun stop() {
        wasPlayingBeforeInterruption = false
        finaliseSession()
        engine.stop()
        focus.abandon()
        appContext.stopService(Intent(appContext, TherapyAudioService::class.java))
    }

    private fun onFocusChange(change: Int) {
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(tag, "focus LOSS → full stop")
                wasPlayingBeforeInterruption = false
                finaliseSession()
                engine.stop()
                appContext.stopService(Intent(appContext, TherapyAudioService::class.java))
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (engine.isPlayingFlow.value) {
                    Log.d(tag, "focus LOSS_TRANSIENT → pause engine, keep service")
                    wasPlayingBeforeInterruption = true
                    // Session stays open — a transient loss does not split a session.
                    _liveSession.update { it?.copy(isPaused = true) }
                    engine.stop()
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (wasPlayingBeforeInterruption) {
                    Log.d(tag, "focus GAIN after transient loss → resume engine")
                    wasPlayingBeforeInterruption = false
                    _liveSession.update { it?.copy(isPaused = false) }
                    engine.start()
                }
            }
        }
    }

    private fun finaliseSession() {
        val start: Long
        val end = System.currentTimeMillis()
        val segmentsToLog: List<SessionSegmentDraft>
        val dominantPresetName: String?

        synchronized(this) {
            start = currentSessionStartedAtMs ?: return
            currentSessionStartedAtMs = null
            
            // Finalize the active segment
            currentSegment?.let { seg ->
                val duration = end - seg.startedAtEpochMs
                if (duration > 0) {
                    currentSessionSegments.add(SessionSegmentDraft(seg.startedAtEpochMs, duration, seg.presetName))
                }
            }
            currentSegment = null
            
            segmentsToLog = currentSessionSegments.toList()
            currentSessionSegments.clear()
            
            // Find the preset with the most duration for backwards compat
            val presetDurations = mutableMapOf<String, Long>()
            for (seg in segmentsToLog) {
                if (seg.presetName != null) {
                    presetDurations[seg.presetName] = presetDurations.getOrDefault(seg.presetName, 0L) + seg.durationMs
                }
            }
            dominantPresetName = presetDurations.maxByOrNull { it.value }?.key
        }

        _liveSession.value = null
        
        ioScope.launch {
            sessionRepo.logSession(
                startedAtEpochMs = start,
                endedAtEpochMs = end,
                segments = segmentsToLog,
                presetLabel = dominantPresetName
            )?.also {
                Log.d(
                    tag,
                    "logged session id=$it duration=${end - start}ms " +
                        "dominantPreset=$dominantPresetName segmentCount=${segmentsToLog.size}"
                )
            }
        }
    }
}


