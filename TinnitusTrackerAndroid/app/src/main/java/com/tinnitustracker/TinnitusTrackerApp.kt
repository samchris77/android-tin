package com.tinnitustracker

import android.app.Application
import com.tinnitustracker.audio.engine.AudioEngine

class TinnitusTrackerApp : Application() {

    // Process-scoped AudioEngine. Lives until the OS kills the process.
    // Owned here (not by MainActivity or the foreground service) so that
    // playback survives Activity recreation and the engine is the single
    // source of truth for both UI (via AudioRepository) and notification
    // (via TherapyAudioService).
    val audioEngine: AudioEngine by lazy { AudioEngine(this) }
}
