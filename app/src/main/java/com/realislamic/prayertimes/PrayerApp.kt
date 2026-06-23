package com.realislamic.prayertimes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.util.LocaleHelper

class PrayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val prefs = PreferencesManager(this)
        LocaleHelper.applyLocale(this, prefs.languageCode)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ALERTS,
                getString(R.string.settings_notification_per_prayer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Azan notifications for each of the five daily prayers"
                enableVibration(true)
                setSound(null, null)
            }

            manager.createNotificationChannel(prayerChannel)
        }
    }

    companion object {
        const val CHANNEL_PRAYER_ALERTS = "prayer_alerts_channel"
    }
}
