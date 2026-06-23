package com.realislamic.prayertimes.ui.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.data.local.PrayerTrackerEntity
import com.realislamic.prayertimes.databinding.FragmentTrackerBinding
import com.realislamic.prayertimes.databinding.ItemTrackerRowBinding

class TrackerFragment : Fragment() {

    private var _binding: FragmentTrackerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackerViewModel by viewModels()

    private val prayerLabels = mapOf(
        "FAJR" to R.string.fajr,
        "DHUHR" to R.string.dhuhr,
        "ASR" to R.string.asr,
        "MAGHRIB" to R.string.maghrib,
        "ISHA" to R.string.isha
    )

    private val prayerIcons = mapOf(
        "FAJR" to R.drawable.ic_moon,
        "DHUHR" to R.drawable.ic_sun,
        "ASR" to R.drawable.ic_sun,
        "MAGHRIB" to R.drawable.ic_sunset,
        "ISHA" to R.drawable.ic_moon
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.todayRecords.observe(viewLifecycleOwner) { records ->
            renderChecklist(records)
        }
    }

    private fun renderChecklist(records: List<PrayerTrackerEntity>) {
        binding.trackerChecklist.removeAllViews()

        val completedMap = records.associateBy { it.prayerName }
        var completedCount = 0

        for (prayerKey in viewModel.prayerKeys) {
            val record = completedMap[prayerKey]
            val isCompleted = record?.isCompleted == true
            if (isCompleted) completedCount++

            val rowBinding = ItemTrackerRowBinding.inflate(
                layoutInflater, binding.trackerChecklist, false
            )

            rowBinding.tvTrackerPrayerName.setText(prayerLabels[prayerKey] ?: R.string.fajr)
            rowBinding.ivTrackerIcon.setImageResource(prayerIcons[prayerKey] ?: R.drawable.ic_moon)
            rowBinding.ivTrackerCheck.setImageResource(
                if (isCompleted) R.drawable.ic_check_circle else R.drawable.ic_circle_outline
            )

            rowBinding.root.setOnClickListener {
                viewModel.toggleCompletion(prayerKey, isCompleted)
            }

            binding.trackerChecklist.addView(rowBinding.root)
        }

        binding.progressRingToday.progress = completedCount
        binding.tvTrackerFractionLarge.text = "$completedCount/5"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
