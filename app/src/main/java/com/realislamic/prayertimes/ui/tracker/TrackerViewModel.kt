package com.realislamic.prayertimes.ui.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.realislamic.prayertimes.data.local.AppDatabase
import com.realislamic.prayertimes.data.local.PrayerTrackerEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).prayerTrackerDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val todayKey: String get() = dateFormat.format(Date())

    val todayRecords: LiveData<List<PrayerTrackerEntity>> =
        dao.getRecordsForDate(todayKey).asLiveData()

    val prayerKeys = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")

    fun toggleCompletion(prayerName: String, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            val existing = dao.getRecord(todayKey, prayerName)
            val entity = PrayerTrackerEntity(
                id = existing?.id ?: 0,
                dateKey = todayKey,
                prayerName = prayerName,
                isCompleted = !currentlyCompleted,
                completedAtEpochMillis = if (!currentlyCompleted) System.currentTimeMillis() else null
            )
            dao.upsert(entity)
        }
    }
}
