package com.realislamic.prayertimes.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.data.local.PreferencesManager

/**
 * Fires when an AlarmManager prayer alarm triggers.
 * Responsibilities:
 *  1. Auto-silence the device if the user has enabled that preference.
 *  2. Start AzanPlaybackService as a foreground service to play the azan audio.
 *  3. Show a system notification for the prayer.
 *  4. Schedule a SilentModeRestoreReceiver alarm to restore the ringer after X minutes.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PRAYER_KEY = "prayer_key"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerKey = intent.getStringExtra(EXTRA_PRAYER_KEY) ?: "prayer"
        val prefs = PreferencesManager(context)

        // 1. Auto silent mode
        if (prefs.autoSilentModeEnabled) {
            applySilentMode(context, prefs)
        }

        // 2. Start foreground azan playback service
        val playIntent = Intent(context, AzanPlaybackService::class.java).apply {
            putExtra(AzanPlaybackService.EXTRA_PRAYER_KEY, prayerKey)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(playIntent)
        } else {
            context.startService(playIntent)
        }

        // 3. Show notification (the AzanPlaybackService shows its own foreground notification,
        //    but we also post an informational one for when the user dismisses the azan)
        postPrayerNotification(context, prayerKey)
    }

    private fun applySilentMode(context: Context, prefs: PreferencesManager) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Save current ringer mode so we can restore it later
        prefs.previousRingerMode = audioManager.ringerMode

        // Use DND access if available, otherwise fall back to RINGER_MODE_SILENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            notificationManager.isNotificationPolicyAccessGranted
        ) {
            notificationManager.setInterruptionFilter(
                NotificationManager.INTERRUPTION_FILTER_NONE
            )
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }

        // Schedule the restore alarm after the configured duration
        SilentModeRestoreReceiver.schedule(context, prefs.silentDurationMinutes)
    }

    private fun postPrayerNotification(context: Context, prayerKey: String) {
        val prayerLabel = when (prayerKey) {
            "fajr" -> context.getString(R.string.fajr)
            "dhuhr" -> context.getString(R.string.dhuhr)
            "asr" -> context.getString(R.string.asr)
            "maghrib" -> context.getString(R.string.maghrib)
            "isha" -> context.getString(R.string.isha)
            else -> prayerKey
        }

        val builder = android.app.Notification.Builder(context,
            com.realislamic.prayertimes.PrayerApp.CHANNEL_PRAYER_ALERTS)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle("اذان • $prayerLabel")
            .setContentText(context.getString(R.string.time_remaining))
            .setAutoCancel(true)

        val notifManager = context.getSystemService(NotificationManager::class.java)
        notifManager.notify(prayerKey.hashCode(), builder.build())
    }
}
