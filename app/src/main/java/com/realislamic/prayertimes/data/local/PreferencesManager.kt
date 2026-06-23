package com.realislamic.prayertimes.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.realislamic.prayertimes.data.model.PrayerTimesData

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    var lastLatitude: Double
        get() = prefs.getFloat(KEY_LAT, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_LAT, value.toFloat()).apply()

    var lastLongitude: Double
        get() = prefs.getFloat(KEY_LNG, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_LNG, value.toFloat()).apply()

    var lastLocationName: String
        get() = prefs.getString(KEY_LOCATION_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LOCATION_NAME, value).apply()

    var lastCountryCode: String?
        get() = prefs.getString(KEY_COUNTRY_CODE, null)
        set(value) = prefs.edit().putString(KEY_COUNTRY_CODE, value).apply()

    fun hasLastKnownLocation(): Boolean =
        prefs.contains(KEY_LAT) && prefs.contains(KEY_LNG)

    fun cachePrayerTimes(data: PrayerTimesData) {
        prefs.edit().putString(KEY_CACHED_TIMINGS, gson.toJson(data)).apply()
    }

    fun getCachedPrayerTimes(): PrayerTimesData? {
        val json = prefs.getString(KEY_CACHED_TIMINGS, null) ?: return null
        return try {
            gson.fromJson(json, PrayerTimesData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    var calculationMethodId: Int
        get() = prefs.getInt(KEY_CALC_METHOD, -1)
        set(value) = prefs.edit().putInt(KEY_CALC_METHOD, value).apply()

    var isCalculationMethodManuallySet: Boolean
        get() = prefs.getBoolean(KEY_CALC_METHOD_MANUAL, false)
        set(value) = prefs.edit().putBoolean(KEY_CALC_METHOD_MANUAL, value).apply()

    var hijriOffsetDays: Int
        get() = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_HIJRI_OFFSET, value.coerceIn(-2, 2)).apply()

    var languageCode: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    fun isPrayerNotificationEnabled(prayerKey: String): Boolean =
        prefs.getBoolean("$KEY_PRAYER_NOTIF_PREFIX$prayerKey", true)

    fun setPrayerNotificationEnabled(prayerKey: String, enabled: Boolean) {
        prefs.edit().putBoolean("$KEY_PRAYER_NOTIF_PREFIX$prayerKey", enabled).apply()
    }

    var autoSilentModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SILENT_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SILENT_ENABLED, value).apply()

    var silentDurationMinutes: Int
        get() = prefs.getInt(KEY_SILENT_DURATION, 20)
        set(value) = prefs.edit().putInt(KEY_SILENT_DURATION, value.coerceIn(1, 120)).apply()

    var previousRingerMode: Int
        get() = prefs.getInt(KEY_PREV_RINGER_MODE, -1)
        set(value) = prefs.edit().putInt(KEY_PREV_RINGER_MODE, value).apply()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    var azanSoundPath: String
        get() = prefs.getString(KEY_AZAN_SOUND_PATH, AZAN_DEFAULT) ?: AZAN_DEFAULT
        set(value) = prefs.edit().putString(KEY_AZAN_SOUND_PATH, value).apply()

    companion object {
        private const val PREFS_NAME = "prayer_times_prefs"
        private const val KEY_LAT = "lat"
        private const val KEY_LNG = "lng"
        private const val KEY_LOCATION_NAME = "location_name"
        private const val KEY_COUNTRY_CODE = "country_code"
        private const val KEY_CACHED_TIMINGS = "cached_timings"
        private const val KEY_CALC_METHOD = "calc_method"
        private const val KEY_CALC_METHOD_MANUAL = "calc_method_manual"
        private const val KEY_HIJRI_OFFSET = "hijri_offset"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_PRAYER_NOTIF_PREFIX = "notif_prayer_"
        private const val KEY_AUTO_SILENT_ENABLED = "auto_silent_enabled"
        private const val KEY_SILENT_DURATION = "silent_duration"
        private const val KEY_PREV_RINGER_MODE = "prev_ringer_mode"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_AZAN_SOUND_PATH = "azan_sound_path"
        const val AZAN_DEFAULT = "default"
    }
}
