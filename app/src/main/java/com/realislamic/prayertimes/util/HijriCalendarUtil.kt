package com.realislamic.prayertimes.util

import java.util.Calendar
import java.util.GregorianCalendar

object HijriCalendarUtil {

    private val HIJRI_MONTH_NAMES_EN = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val HIJRI_MONTH_NAMES_UR = arrayOf(
        "محرم", "صفر", "ربیع الاول", "ربیع الثانی",
        "جمادی الاول", "جمادی الثانی", "رجب", "شعبان",
        "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ"
    )

    data class HijriResult(
        val day: Int,
        val month: Int,
        val year: Int,
        val monthNameEn: String,
        val monthNameUr: String
    )

    fun gregorianToHijri(calendar: Calendar, offsetDays: Int = 0): HijriResult {
        val adjusted = calendar.clone() as Calendar
        adjusted.add(Calendar.DAY_OF_MONTH, offsetDays)

        val jd = gregorianToJulianDay(
            adjusted.get(Calendar.YEAR),
            adjusted.get(Calendar.MONTH) + 1,
            adjusted.get(Calendar.DAY_OF_MONTH)
        )

        val (hYear, hMonth, hDay) = julianDayToHijri(jd)

        return HijriResult(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameEn = HIJRI_MONTH_NAMES_EN[(hMonth - 1).coerceIn(0, 11)],
            monthNameUr = HIJRI_MONTH_NAMES_UR[(hMonth - 1).coerceIn(0, 11)]
        )
    }

    fun today(offsetDays: Int = 0): HijriResult = gregorianToHijri(GregorianCalendar(), offsetDays)

    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Long {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return (day + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045).toLong()
    }

    private fun julianDayToHijri(jd: Long): Triple<Int, Int, Int> {
        val islamicEpoch = 1948440L
        val daysSinceEpoch = jd - islamicEpoch + 1

        val cycles = (daysSinceEpoch - 1) / 10631
        var remainingDays = (daysSinceEpoch - 1) % 10631
        if (remainingDays < 0) remainingDays += 10631

        var year = (cycles * 30).toInt()

        val yearLengths = intArrayOf(354, 354, 354, 355, 354, 354, 355, 354, 355, 355,
            354, 354, 355, 354, 354, 355, 354, 355, 355, 354,
            355, 354, 354, 355, 354, 354, 355, 354, 354, 355)

        var yIndex = 0
        var daysLeft = remainingDays
        while (yIndex < 30 && daysLeft >= yearLengths[yIndex]) {
            daysLeft -= yearLengths[yIndex]
            yIndex++
        }
        year += yIndex + 1

        val monthLengths = intArrayOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29)
        var month = 0
        while (month < 11 && daysLeft >= monthLengths[month]) {
            daysLeft -= monthLengths[month]
            month++
        }

        val day = (daysLeft + 1).toInt()
        return Triple(year, month + 1, day)
    }
}
