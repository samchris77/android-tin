package com.tinnitustracker.audio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tinnitustracker.MainActivity
import com.tinnitustracker.R

object AudioNotification {

    const val CHANNEL_ID = "therapy_audio"
    private const val CHANNEL_NAME = "이명 치료 재생"
    const val NOTIF_ID = 1001

    private const val REQ_CONTENT = 100
    private const val REQ_STOP    = 101

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW).apply {
                description = "이명 치료 사운드를 재생하는 동안 표시됩니다."
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
        )
    }

    fun build(context: Context, isPlaying: Boolean): android.app.Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context, REQ_CONTENT, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // The action toggles between PAUSE and RESUME
        val actionIntent = Intent(context, TherapyAudioService::class.java).apply {
            action = if (isPlaying) TherapyAudioService.ACTION_PAUSE else TherapyAudioService.ACTION_RESUME
        }
        val actionPi = PendingIntent.getService(
            context, REQ_STOP, actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val actionLabel = if (isPlaying) "일시정지" else "재생"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_wave)
            .setContentTitle("이명 치료 재생 중")
            .setContentText("탭하면 앱으로 돌아갑니다.")
            .setContentIntent(contentPi)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(actionIcon, actionLabel, actionPi)
            .build()
    }
}
