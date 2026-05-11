package com.tinnitustracker.audio.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.tinnitustracker.TinnitusTrackerApp
import com.tinnitustracker.audio.engine.AudioEngine

/**
 * Foreground service that keeps the audio process alive while therapy is playing.
 * The actual `AudioEngine` lives in [TinnitusTrackerApp] — this service only owns
 * the foreground notification + lifecycle. `AudioRepository` starts/stops the
 * service in lock-step with engine playback; the notification's "정지" action
 * routes back here via [ACTION_STOP].
 */
class TherapyAudioService : Service() {

    private val engine: AudioEngine
        get() = (application as TinnitusTrackerApp).audioEngine

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AudioNotification.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(AudioNotification.NOTIF_ID, AudioNotification.build(this))
            }
            ACTION_STOP -> {
                engine.stop() // idempotent — guarded inside AudioEngine
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    companion object {
        const val ACTION_START = "com.tinnitustracker.audio.action.START"
        const val ACTION_STOP  = "com.tinnitustracker.audio.action.STOP"
    }
}
