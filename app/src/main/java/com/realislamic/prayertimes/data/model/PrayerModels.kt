package com.realislamic.prayertimes.data.model

import com.google.gson.annotations.SerializedName

data class AlAdhanResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: AlAdhanData?
)

data class AlAdhanData(
    @SerializedName("timings") val timings: Timings,
    @SerializedName("date") val date: DateInfo,
    @SerializedName("meta") val meta: Meta
)

data class Timings(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Sunset") val sunset: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String,
    @SerializedName("Imsak") val imsak: String,
    @SerializedName("Midnight") val midnight: String
)

data class DateInfo(
    @SerializedName("readable") val readable: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("gregorian") val gregorian: GregorianDate,
    @SerializedName("hijri") val hijri: HijriDate
)

data class GregorianDate(
    @SerializedName("date") val date: String,
    @SerializedName("day") val day: String,
    @SerializedName("month") val month: GregorianMonth,
    @SerializedName("year") val year: String,
    @SerializedName("weekday") val weekday: Weekday
)

data class GregorianMonth(
    @SerializedName("number") val number: Int,
    @SerializedName("en") val en: String
)

data class Weekday(
    @SerializedName("en") val en: String
)

data class HijriDate(
    @SerializedName("date") val date: String,
    @SerializedName("day") val day: String,
    @SerializedName("month") val month: HijriMonth,
    @SerializedName("year") val year: String,
    @SerializedName("weekday") val weekday: HijriWeekday
)

data class HijriMonth(
    @SerializedName("number") val number: Int,
    @SerializedName("en") val en: String,
    @SerializedName("ar") val ar: String
)

data class HijriWeekday(
    @SerializedName("en") val en: String,
    @SerializedName("ar") val ar: String
)

data class Meta(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("method") val method: CalculationMethodInfo
)

data class CalculationMethodInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class PrayerTimesData(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDay: String,
    val hijriMonthEn: String,
    val hijriMonthAr: String,
    val hijriYear: String,
    val gregorianDateReadable: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val calculationMethodId: Int,
    val fetchedAtEpochMillis: Long
)

enum class PrayerName {
    FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
}
