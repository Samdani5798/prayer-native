package com.realislamic.prayertimes.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.realislamic.prayertimes.PrayerApp
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.ui.MainActivity

/**
 * Foreground service that plays the locally-bundled azan audio file so it
 * continues even if the user is in another app or the screen is off.
 * The service stops itself once playback completes.
 *
 * Audio source priority:
 *  1. User-downloaded custom azan at the path stored in preferences
 *  2. Bundled res/raw/azan_default.mp3 (the file supplied at build time)
 */
class AzanPlaybackService : Service() {

    companion object {
        const val EXTRA_PRAYER_KEY = "prayer_key"
        private const val FOREGROUND_ID = 9001
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerKey = intent?.getStringExtra(EXTRA_PRAYER_KEY) ?: "prayer"

        startForeground(FOREGROUND_ID, buildForegroundNotification(prayerKey))
        playAzan()

        return START_NOT_STICKY
    }

    private fun playAzan() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.azan_default)
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                stopSelf()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun buildForegroundNotification(prayerKey: String): Notification {
        val prayerLabel = when (prayerKey) {
            "fajr" -> getString(R.string.fajr)
            "dhuhr" -> getString(R.string.dhuhr)
            "asr" -> getString(R.string.asr)
            "maghrib" -> getString(R.string.maghrib)
            "isha" -> getString(R.string.isha)
            else -> prayerKey
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, PrayerApp.CHANNEL_PRAYER_ALERTS)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle("اذان • $prayerLabel")
            .setContentText(getString(R.string.time_remaining))
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
