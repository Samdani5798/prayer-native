package com.realislamic.prayertimes.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.data.model.CalculationMethod
import com.realislamic.prayertimes.data.model.PrayerTimesData
import com.realislamic.prayertimes.data.remote.NetworkModule
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class PrayerTimesResult {
    data class Success(val data: PrayerTimesData, val isFromCache: Boolean) : PrayerTimesResult()
    data class Error(val message: String, val cachedFallback: PrayerTimesData?) : PrayerTimesResult()
}

class PrayerTimesRepository(private val context: Context) {

    private val prefsManager = PreferencesManager(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val cancellationTokenSource = CancellationTokenSource()
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(60_000)
            .build()

        fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (cont.isActive) cont.resume(location)
            }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }

        cont.invokeOnCancellation { cancellationTokenSource.cancel() }
    }

    fun reverseGeocode(lat: Double, lng: Double): Pair<String, String?> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val address = addresses?.firstOrNull()
            val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea
            val country = address?.countryName
            val countryCode = address?.countryCode
            val displayName = listOfNotNull(city, country).joinToString(", ")
            Pair(displayName.ifBlank { "Unknown location" }, countryCode)
        } catch (e: Exception) {
            Pair("Unknown location", null)
        }
    }

    suspend fun refreshPrayerTimes(forceLocationUpdate: Boolean = true): PrayerTimesResult {
        val cached = prefsManager.getCachedPrayerTimes()

        try {
            val location: Location? = if (forceLocationUpdate) {
                try {
                    fetchCurrentLocation()
                } catch (e: Exception) {
                    null
                }
            } else null

            val lat: Double
            val lng: Double

            if (location != null) {
                lat = location.latitude
                lng = location.longitude
                prefsManager.lastLatitude = lat
                prefsManager.lastLongitude = lng
            } else if (prefsManager.hasLastKnownLocation()) {
                lat = prefsManager.lastLatitude
                lng = prefsManager.lastLongitude
            } else {
                return PrayerTimesResult.Error("No location available", cached)
            }

            val (placeName, countryCode) = reverseGeocode(lat, lng)
            prefsManager.lastLocationName = placeName
            prefsManager.lastCountryCode = countryCode

            val methodId = if (prefsManager.isCalculationMethodManuallySet && prefsManager.calculationMethodId >= 0) {
                prefsManager.calculationMethodId
            } else {
                val auto = CalculationMethod.forCountryCode(countryCode)
                prefsManager.calculationMethodId = auto.id
                auto.id
            }

            val response = NetworkModule.alAdhanApi.getTimingsByCoordinates(lat, lng, methodId)

            if (response.isSuccessful) {
                val body = response.body()
                val timings = body?.data?.timings
                val dateInfo = body?.data?.date

                if (timings != null && dateInfo != null) {
                    val data = PrayerTimesData(
                        fajr = timings.fajr.take(5),
                        sunrise = timings.sunrise.take(5),
                        dhuhr = timings.dhuhr.take(5),
                        asr = timings.asr.take(5),
                        maghrib = timings.maghrib.take(5),
                        isha = timings.isha.take(5),
                        hijriDay = dateInfo.hijri.day,
                        hijriMonthEn = dateInfo.hijri.month.en,
                        hijriMonthAr = dateInfo.hijri.month.ar,
                        hijriYear = dateInfo.hijri.year,
                        gregorianDateReadable = dateInfo.readable,
                        locationName = placeName,
                        latitude = lat,
                        longitude = lng,
                        calculationMethodId = methodId,
                        fetchedAtEpochMillis = System.currentTimeMillis()
                    )
                    prefsManager.cachePrayerTimes(data)
                    return PrayerTimesResult.Success(data, isFromCache = false)
                }
            }
            return PrayerTimesResult.Error("API returned an error", cached)
        } catch (e: Exception) {
            return if (cached != null) {
                PrayerTimesResult.Success(cached, isFromCache = true)
            } else {
                PrayerTimesResult.Error(e.message ?: "Unknown error", null)
            }
        }
    }

    fun getCachedPrayerTimes(): PrayerTimesData? = prefsManager.getCachedPrayerTimes()
}
