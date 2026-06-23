package com.realislamic.prayertimes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTrackerDao {

    @Query("SELECT * FROM prayer_tracker WHERE dateKey = :dateKey")
    fun getRecordsForDate(dateKey: String): Flow<List<PrayerTrackerEntity>>

    @Query("SELECT * FROM prayer_tracker WHERE dateKey = :dateKey AND prayerName = :prayerName LIMIT 1")
    suspend fun getRecord(dateKey: String, prayerName: String): PrayerTrackerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PrayerTrackerEntity)

    @Update
    suspend fun update(entity: PrayerTrackerEntity)

    @Query("SELECT * FROM prayer_tracker WHERE dateKey BETWEEN :startDate AND :endDate ORDER BY dateKey ASC")
    fun getRecordsInRange(startDate: String, endDate: String): Flow<List<PrayerTrackerEntity>>

    @Query("SELECT COUNT(*) FROM prayer_tracker WHERE dateKey = :dateKey AND isCompleted = 1")
    suspend fun getCompletedCountForDate(dateKey: String): Int

    @Query("DELETE FROM prayer_tracker WHERE dateKey < :beforeDateKey")
    suspend fun pruneOlderThan(beforeDateKey: String)
}
