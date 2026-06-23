package com.realislamic.prayertimes.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.databinding.ActivityMainBinding
import com.realislamic.prayertimes.ui.calendar.CalendarFragment
import com.realislamic.prayertimes.ui.home.HomeFragment
import com.realislamic.prayertimes.ui.qibla.QiblaFragment
import com.realislamic.prayertimes.ui.settings.SettingsFragment
import com.realislamic.prayertimes.ui.tracker.TrackerFragment

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentTabId: Int = R.id.navHome

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showFragment(HomeFragment(), R.id.navHome)
        }

        binding.navTracker.setOnClickListener { showFragment(TrackerFragment(), R.id.navTracker) }
        binding.navQibla.setOnClickListener { showFragment(QiblaFragment(), R.id.navQibla) }
        binding.navHome.setOnClickListener { showFragment(HomeFragment(), R.id.navHome) }
        binding.navCalendar.setOnClickListener { showFragment(CalendarFragment(), R.id.navCalendar) }
        binding.navSettings.setOnClickListener { showFragment(SettingsFragment(), R.id.navSettings) }
    }

    private fun showFragment(fragment: Fragment, tabId: Int) {
        if (currentTabId == tabId && supportFragmentManager.findFragmentById(R.id.fragmentContainer) != null) {
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        currentTabId = tabId
        updateTabHighlight(tabId)
    }

    private fun updateTabHighlight(activeTabId: Int) {
        val tabs = listOf(
            Triple(binding.navTracker, binding.iconTracker, binding.labelTracker),
            Triple(binding.navQibla, binding.iconQibla, binding.labelQibla),
            Triple(binding.navCalendar, binding.iconCalendar, binding.labelCalendar),
            Triple(binding.navSettings, binding.iconSettings, binding.labelSettings)
        )

        tabs.forEach { (container, icon, label) ->
            container.background = null
            icon.imageTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.text_secondary, theme)
            )
            label.setTextColor(resources.getColor(R.color.text_secondary, theme))
        }

        if (activeTabId != R.id.navHome) {
            val (_, activeIcon, activeLabel) = when (activeTabId) {
                R.id.navTracker -> tabs[0]
                R.id.navQibla -> tabs[1]
                R.id.navCalendar -> tabs[2]
                R.id.navSettings -> tabs[3]
                else -> tabs[0]
            }
            activeIcon.imageTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.accent_green, theme)
            )
            activeLabel.setTextColor(resources.getColor(R.color.accent_green, theme))
        }
    }
}
