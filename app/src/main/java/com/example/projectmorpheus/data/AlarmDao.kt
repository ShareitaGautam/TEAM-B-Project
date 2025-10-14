package com.example.projectmorpheus.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hourOfDay, minute")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Delete
    suspend fun delete(alarm: Alarm)

    @Update
    suspend fun update(alarm: Alarm)
}
