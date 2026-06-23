package com.realislamic.prayertimes.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.realislamic.prayertimes.databinding.ItemCalendarDayBinding

data class CalendarDayUi(
    val gregorianDay: Int,
    val hijriDay: Int,
    val isToday: Boolean
)

class CalendarDayAdapter(private val days: List<CalendarDayUi>) :
    RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    class DayViewHolder(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.binding.tvDayGregorian.text = day.gregorianDay.toString()
        holder.binding.tvDayHijri.text = day.hijriDay.toString()

        if (day.isToday) {
            holder.binding.root.setBackgroundResource(com.realislamic.prayertimes.R.drawable.bg_prayer_chip_active)
        } else {
            holder.binding.root.background = null
        }
    }

    override fun getItemCount(): Int = days.size
}
