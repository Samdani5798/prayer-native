package com.realislamic.prayertimes.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.realislamic.prayertimes.data.PrayerTimesRepository
import com.realislamic.prayertimes.data.PrayerTimesResult
import com.realislamic.prayertimes.data.model.PrayerTimesData
import com.realislamic.prayertimes.notification.AlarmScheduler
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerTimesRepository(application)

    private val _prayerTimes = MutableLiveData<PrayerTimesData?>()
    val prayerTimes: LiveData<PrayerTimesData?> = _prayerTimes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFromCache = MutableLiveData(false)
    val isFromCache: LiveData<Boolean> = _isFromCache

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // Show cached data instantly so UI is never blank
        _prayerTimes.value = repository.getCachedPrayerTimes()
        refresh()
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.refreshPrayerTimes()) {
                is PrayerTimesResult.Success -> {
                    _prayerTimes.value = result.data
                    _isFromCache.value = result.isFromCache
                    _error.value = null
                    // Reschedule AlarmManager alarms with fresh data (offline-first: even if
                    // from cache, still reschedule so they survive app restarts)
                    AlarmScheduler.scheduleTodayPrayers(getApplication(), result.data)
                }
                is PrayerTimesResult.Error -> {
                    if (result.cachedFallback != null) {
                        _prayerTimes.value = result.cachedFallback
                        _isFromCache.value = true
                        // Still reschedule from cache so offline-first still works
                        AlarmScheduler.scheduleTodayPrayers(getApplication(), result.cachedFallback)
                    }
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }
}
