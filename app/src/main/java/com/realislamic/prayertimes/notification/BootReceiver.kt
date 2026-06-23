package com.realislamic.prayertimes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.realislamic.prayertimes.data.PrayerTimesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all prayer alarms after the device is rebooted, since
 * AlarmManager alarms are cleared when the device powers off.
 * Uses the last cached prayer times to reschedule without needing internet.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        // Use cached prayer times to reschedule — no internet needed
        CoroutineScope(Dispatchers.IO).launch {
            val repository = PrayerTimesRepository(context)
            val cached = repository.getCachedPrayerTimes()
            if (cached != null) {
                AlarmScheduler.scheduleTodayPrayers(context, cached)
            }
        }
    }
}
