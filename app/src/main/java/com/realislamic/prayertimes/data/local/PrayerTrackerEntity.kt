package com.realislamic.prayertimes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_tracker")
data class PrayerTrackerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val prayerName: String,
    val isCompleted: Boolean,
    val completedAtEpochMillis: Long?
)
