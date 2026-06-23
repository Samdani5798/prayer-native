package com.realislamic.prayertimes.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.databinding.FragmentCalendarBinding
import com.realislamic.prayertimes.util.HijriCalendarUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager
    private var displayedMonth: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        binding.recyclerCalendarDays.layoutManager = GridLayoutManager(requireContext(), 7)

        binding.tvOffsetValue.text = prefs.hijriOffsetDays.toString()

        binding.btnOffsetMinus.setOnClickListener {
            prefs.hijriOffsetDays = (prefs.hijriOffsetDays - 1).coerceIn(-2, 2)
            binding.tvOffsetValue.text = prefs.hijriOffsetDays.toString()
            renderMonth()
        }

        binding.btnOffsetPlus.setOnClickListener {
            prefs.hijriOffsetDays = (prefs.hijriOffsetDays + 1).coerceIn(-2, 2)
            binding.tvOffsetValue.text = prefs.hijriOffsetDays.toString()
            renderMonth()
        }

        binding.btnPrevMonth.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, -1)
            renderMonth()
        }

        binding.btnNextMonth.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, 1)
            renderMonth()
        }

        renderMonth()
    }

    private fun renderMonth() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvGregorianMonthYear.text = monthFormat.format(displayedMonth.time)

        val today = Calendar.getInstance()
        val offset = prefs.hijriOffsetDays

        val todayHijri = HijriCalendarUtil.gregorianToHijri(today, offset)
        binding.tvHijriMonthYear.text = "${todayHijri.monthNameEn} ${todayHijri.year}"

        val daysInMonth = displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysList = mutableListOf<CalendarDayUi>()

        for (day in 1..daysInMonth) {
            val cal = displayedMonth.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, day)
            val hijri = HijriCalendarUtil.gregorianToHijri(cal, offset)

            val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

            daysList.add(CalendarDayUi(gregorianDay = day, hijriDay = hijri.day, isToday = isToday))
        }

        binding.recyclerCalendarDays.adapter = CalendarDayAdapter(daysList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
