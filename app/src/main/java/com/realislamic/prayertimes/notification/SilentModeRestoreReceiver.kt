package com.realislamic.prayertimes.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import com.realislamic.prayertimes.data.local.PreferencesManager

/**
 * Restores the device's ringer mode to what it was before the prayer alarm
 * auto-silenced it. Triggered by a delayed AlarmManager alarm set by PrayerAlarmReceiver.
 */
class SilentModeRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferencesManager(context)
        val prevMode = prefs.previousRingerMode
        if (prevMode < 0) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val audioManager = context.getSystemService(AudioManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            notificationManager.isNotificationPolicyAccessGranted
        ) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } else {
            audioManager.ringerMode = prevMode
        }

        prefs.previousRingerMode = -1
    }

    companion object {
        private const val REQUEST_CODE = 7777

        fun schedule(context: Context, delayMinutes: Int) {
            val intent = Intent(context, SilentModeRestoreReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
