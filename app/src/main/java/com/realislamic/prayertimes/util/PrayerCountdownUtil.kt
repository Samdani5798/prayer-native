package com.realislamic.prayertimes.util

import com.realislamic.prayertimes.data.model.PrayerTimesData
import java.util.Calendar

/**
 * Determines the next upcoming prayer from "now" and the live countdown progress
 * (0f at the start of the gap since the previous prayer, 1f right as the next
 * prayer begins) — this progress value directly drives the DomeLiquidView fill.
 */
object PrayerCountdownUtil {

    data class NextPrayerInfo(
        val prayerKey: String, // "fajr", "dhuhr", "asr", "maghrib", "isha"
        val prayerTimeMillis: Long,
        val previousPrayerTimeMillis: Long,
        val remainingMillis: Long
    ) {
        val progress: Float
            get() {
                val total = (prayerTimeMillis - previousPrayerTimeMillis).coerceAtLeast(1L)
                val elapsed = total - remainingMillis
                return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
    }

    /**
     * Parses "HH:mm" into a Calendar for today (or tomorrow if needed) based on current time.
     */
    private fun timeStringToCalendarToday(time: String): Calendar {
        val parts = time.split(":")
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 0)
        cal.set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun getNextPrayer(data: PrayerTimesData): NextPrayerInfo {
        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis

        // Ordered list of (key, time) for the five daily prayers
        val prayers = listOf(
            "fajr" to data.fajr,
            "dhuhr" to data.dhuhr,
            "asr" to data.asr,
            "maghrib" to data.maghrib,
            "isha" to data.isha
        )

        val todayTimes = prayers.map { (key, time) -> key to timeStringToCalendarToday(time).timeInMillis }

        // Find first prayer time today that's still in the future
        val nextToday = todayTimes.firstOrNull { it.second > nowMillis }

        return if (nextToday != null) {
            val index = todayTimes.indexOf(nextToday)
            val previousMillis = if (index == 0) {
                // Previous prayer was yesterday's Isha; approximate as 6 hours before today's Fajr
                todayTimes[0].second - (6 * 60 * 60 * 1000L)
            } else {
                todayTimes[index - 1].second
            }
            NextPrayerInfo(
                prayerKey = nextToday.first,
                prayerTimeMillis = nextToday.second,
                previousPrayerTimeMillis = previousMillis,
                remainingMillis = nextToday.second - nowMillis
            )
        } else {
            // All of today's prayers have passed; next is tomorrow's Fajr
            val tomorrowFajrCal = timeStringToCalendarToday(data.fajr)
            tomorrowFajrCal.add(Calendar.DAY_OF_MONTH, 1)
            val tomorrowFajrMillis = tomorrowFajrCal.timeInMillis
            val previousMillis = todayTimes.last().second // today's Isha

            NextPrayerInfo(
                prayerKey = "fajr",
                prayerTimeMillis = tomorrowFajrMillis,
                previousPrayerTimeMillis = previousMillis,
                remainingMillis = tomorrowFajrMillis - nowMillis
            )
        }
    }

    fun formatCountdown(remainingMillis: Long): String {
        val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
