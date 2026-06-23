package com.realislamic.prayertimes.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.data.model.PrayerTimesData
import java.util.Calendar

/**
 * Schedules exact AlarmManager alarms for every prayer time using
 * setExactAndAllowWhileIdle so they fire even in Doze mode.
 * Called after every successful prayer-times fetch and on BOOT_COMPLETED.
 *
 * Why we use AlarmManager instead of WorkManager here:
 * WorkManager has a minimum delay of ~15 minutes and cannot guarantee
 * exact delivery times — unacceptable for Azan alerts. AlarmManager
 * setExactAndAllowWhileIdle delivers within seconds even on idle devices.
 */
object AlarmScheduler {

    private const val REQ_BASE = 1000

    fun scheduleTodayPrayers(context: Context, data: PrayerTimesData) {
        val prefs = PreferencesManager(context)
        if (!prefs.notificationsEnabled) return

        val prayers = listOf(
            Triple("fajr",   data.fajr,    REQ_BASE + 1),
            Triple("dhuhr",  data.dhuhr,   REQ_BASE + 2),
            Triple("asr",    data.asr,     REQ_BASE + 3),
            Triple("maghrib",data.maghrib, REQ_BASE + 4),
            Triple("isha",   data.isha,    REQ_BASE + 5)
        )

        val now = System.currentTimeMillis()
        for ((key, timeStr, reqCode) in prayers) {
            if (!prefs.isPrayerNotificationEnabled(key)) continue

            val triggerMillis = todayTimeToMillis(timeStr)
            if (triggerMillis > now) {
                scheduleExactAlarm(context, key, triggerMillis, reqCode)
            }
        }
    }

    private fun todayTimeToMillis(hhmm: String): Long {
        val parts = hhmm.split(":")
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 0)
        cal.set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun scheduleExactAlarm(
        context: Context,
        prayerKey: String,
        triggerAtMillis: Long,
        requestCode: Int
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_KEY, prayerKey)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                // Fallback for devices that don't grant exact alarm permission
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val keys = listOf("fajr" to REQ_BASE+1, "dhuhr" to REQ_BASE+2,
            "asr" to REQ_BASE+3, "maghrib" to REQ_BASE+4, "isha" to REQ_BASE+5)
        for ((key, reqCode) in keys) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_KEY, key)
            }
            val pi = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue
            alarmManager.cancel(pi)
        }
    }
}
