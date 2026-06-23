package com.realislamic.prayertimes.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.data.model.PrayerTimesData
import com.realislamic.prayertimes.databinding.FragmentHomeBinding
import com.realislamic.prayertimes.databinding.ItemPrayerCardBinding
import com.realislamic.prayertimes.util.HijriCalendarUtil
import com.realislamic.prayertimes.util.PrayerCountdownUtil
import com.realislamic.prayertimes.util.QiblaUtil

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var prefs: PreferencesManager

    private var countdownTimer: CountDownTimer? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            viewModel.refresh()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        binding.swipeRefresh.setOnRefreshListener {
            ensureLocationPermissionThenRefresh()
        }

        binding.switchAutoSilent.isChecked = prefs.autoSilentModeEnabled
        binding.switchAutoSilent.setOnCheckedChangeListener { _, isChecked ->
            prefs.autoSilentModeEnabled = isChecked
        }

        viewModel.prayerTimes.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                renderPrayerData(data)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }

        ensureLocationPermissionThenRefresh()
    }

    private fun ensureLocationPermissionThenRefresh() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.refresh()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun renderPrayerData(data: PrayerTimesData) {
        // Location name detected live from device GPS + reverse geocoding (never static)
        binding.tvLocationName.text = data.locationName

        binding.tvGregorianDate.text = formatGregorianShort(data)
        binding.tvGregorianFull.text = data.gregorianDateReadable
        binding.tvHijriDateShort.text = "${data.hijriDay} ${data.hijriMonthEn.take(3)}"
        binding.tvHijriFull.text = "${data.hijriDay} ${data.hijriMonthEn} ${data.hijriYear}"

        val qiblaBearing = QiblaUtil.calculateQiblaBearing(data.latitude, data.longitude)
        val cardinal = QiblaUtil.bearingToCardinal(qiblaBearing)
        binding.tvQiblaDegree.text = "${qiblaBearing.toInt()}°"
        binding.tvQiblaCardinal.text = cardinal
        binding.tvQiblaDegreeSmall.text = "${qiblaBearing.toInt()}°"

        populatePrayerRow(data)
        startCountdown(data)
    }

    private fun formatGregorianShort(data: PrayerTimesData): String {
        // gregorianDateReadable is like "21 May 2024"; show as-is, it's already clean
        return data.gregorianDateReadable
    }

    private fun populatePrayerRow(data: PrayerTimesData) {
        binding.prayerRow.removeAllViews()

        val nextInfo = PrayerCountdownUtil.getNextPrayer(data)

        val prayers = listOf(
            Triple("fajr", getString(R.string.fajr), data.fajr) to R.drawable.ic_moon,
            Triple("dhuhr", getString(R.string.dhuhr), data.dhuhr) to R.drawable.ic_sun,
            Triple("asr", getString(R.string.asr), data.asr) to R.drawable.ic_sun,
            Triple("maghrib", getString(R.string.maghrib), data.maghrib) to R.drawable.ic_sunset,
            Triple("isha", getString(R.string.isha), data.isha) to R.drawable.ic_moon
        )

        for ((info, icon) in prayers) {
            val (key, label, time) = info
            val itemBinding = ItemPrayerCardBinding.inflate(
                layoutInflater, binding.prayerRow, false
            )
            itemBinding.tvPrayerName.text = label
            itemBinding.ivPrayerIcon.setImageResource(icon)

            val (hourMin, meridiem) = to12Hour(time)
            itemBinding.tvPrayerTime.text = hourMin
            itemBinding.tvPrayerMeridiem.text = meridiem

            val isActive = key == nextInfo.prayerKey
            if (isActive) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_prayer_chip_active)
                itemBinding.tvPrayerName.setTextColor(
                    resources.getColor(R.color.white, requireContext().theme)
                )
                itemBinding.tvPrayerTime.setTextColor(
                    resources.getColor(R.color.white, requireContext().theme)
                )
                itemBinding.tvPrayerMeridiem.setTextColor(
                    resources.getColor(R.color.white, requireContext().theme)
                )
            }

            binding.prayerRow.addView(itemBinding.root)
        }
    }

    private fun to12Hour(time24: String): Pair<String, String> {
        val parts = time24.split(":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1) ?: "00"
        val meridiem = if (hour >= 12) "PM" else "AM"
        if (hour == 0) hour = 12
        if (hour > 12) hour -= 12
        return Pair(String.format("%02d:%s", hour, minute), meridiem)
    }

    private fun startCountdown(data: PrayerTimesData) {
        countdownTimer?.cancel()

        val nextInfo = PrayerCountdownUtil.getNextPrayer(data)

        binding.tvNextPrayerName.text = when (nextInfo.prayerKey) {
            "fajr" -> getString(R.string.fajr)
            "dhuhr" -> getString(R.string.dhuhr)
            "asr" -> getString(R.string.asr)
            "maghrib" -> getString(R.string.maghrib)
            "isha" -> getString(R.string.isha)
            else -> ""
        }

        binding.ivNextPrayerIcon.setImageResource(
            when (nextInfo.prayerKey) {
                "fajr", "isha" -> R.drawable.ic_moon
                "maghrib" -> R.drawable.ic_sunset
                else -> R.drawable.ic_sun
            }
        )

        binding.domeView.setFillProgress(nextInfo.progress, animate = false)

        val remaining = nextInfo.remainingMillis.coerceAtLeast(0)
        countdownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = PrayerCountdownUtil.formatCountdown(millisUntilFinished)
                val freshInfo = PrayerCountdownUtil.getNextPrayer(data)
                binding.domeView.setFillProgress(freshInfo.progress, animate = true)
            }

            override fun onFinish() {
                // Prayer time reached — refresh to move to the following prayer
                viewModel.refresh()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
        _binding = null
    }
}
