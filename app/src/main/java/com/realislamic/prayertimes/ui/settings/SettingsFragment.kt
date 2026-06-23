package com.realislamic.prayertimes.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.data.model.CalculationMethod
import com.realislamic.prayertimes.databinding.FragmentSettingsBinding
import com.realislamic.prayertimes.databinding.ItemNotificationToggleBinding
import com.realislamic.prayertimes.ui.MainActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager

    private val prayerKeysAndLabels = listOf(
        "fajr" to R.string.fajr,
        "dhuhr" to R.string.dhuhr,
        "asr" to R.string.asr,
        "maghrib" to R.string.maghrib,
        "isha" to R.string.isha
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        setupLanguageToggle()
        setupDarkMode()
        setupNotificationToggles()
        setupAutoSilent()
        setupCalculationMethod()

        binding.tvAppVersion.text = "v1.0.0"
    }

    private fun setupLanguageToggle() {
        if (prefs.languageCode == "ur") {
            binding.languageToggleGroup.check(binding.btnLangUr.id)
        } else {
            binding.languageToggleGroup.check(binding.btnLangEn.id)
        }

        binding.languageToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val newLang = if (checkedId == binding.btnLangUr.id) "ur" else "en"
            if (newLang != prefs.languageCode) {
                prefs.languageCode = newLang
                // Restart MainActivity so the new locale (and RTL layout direction) applies everywhere
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun setupDarkMode() {
        binding.switchDarkMode.isChecked = prefs.darkModeEnabled
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.darkModeEnabled = isChecked
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                if (isChecked) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupNotificationToggles() {
        binding.notificationTogglesContainer.removeAllViews()
        for ((key, labelRes) in prayerKeysAndLabels) {
            val rowBinding = ItemNotificationToggleBinding.inflate(
                layoutInflater, binding.notificationTogglesContainer, false
            )
            rowBinding.tvNotifPrayerName.setText(labelRes)
            rowBinding.switchNotifPrayer.isChecked = prefs.isPrayerNotificationEnabled(key)
            rowBinding.switchNotifPrayer.setOnCheckedChangeListener { _, isChecked ->
                prefs.setPrayerNotificationEnabled(key, isChecked)
            }
            binding.notificationTogglesContainer.addView(rowBinding.root)
        }
    }

    private fun setupAutoSilent() {
        binding.switchSettingsAutoSilent.isChecked = prefs.autoSilentModeEnabled
        binding.silentDurationContainer.visibility =
            if (prefs.autoSilentModeEnabled) View.VISIBLE else View.GONE

        binding.switchSettingsAutoSilent.setOnCheckedChangeListener { _, isChecked ->
            prefs.autoSilentModeEnabled = isChecked
            binding.silentDurationContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val initialMinutes = prefs.silentDurationMinutes
        binding.seekSilentDuration.progress = (initialMinutes - 1).coerceIn(0, 119)
        binding.tvSilentDurationValue.text = "$initialMinutes min"

        binding.seekSilentDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = progress + 1
                binding.tvSilentDurationValue.text = "$minutes min"
                if (fromUser) prefs.silentDurationMinutes = minutes
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupCalculationMethod() {
        val methods = CalculationMethod.entries.toList()
        val currentId = prefs.calculationMethodId
        val current = CalculationMethod.fromId(currentId)
        binding.tvCurrentCalcMethod.text = current.displayName

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            methods.map { it.displayName }
        )
        binding.spinnerCalcMethod.adapter = adapter

        val currentIndex = methods.indexOf(current).coerceAtLeast(0)
        binding.spinnerCalcMethod.setSelection(currentIndex, false)

        binding.spinnerCalcMethod.post {
            binding.spinnerCalcMethod.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selected = methods[position]
                    if (selected.id != prefs.calculationMethodId) {
                        prefs.calculationMethodId = selected.id
                        prefs.isCalculationMethodManuallySet = true
                        binding.tvCurrentCalcMethod.text = selected.displayName
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
