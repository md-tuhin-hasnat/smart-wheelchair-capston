package com.mdtuhinhasnat.smartwheelchairdatacollector.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<SensorReadingEntity>)

    @Transaction
    suspend fun insertSessionWithReadings(session: SessionEntity, readings: List<SensorReadingEntity>) {
        val sessionId = insertSession(session)
        val readingsWithSessionId = readings.map { it.copy(sessionId = sessionId) }
        insertReadings(readingsWithSessionId)
    }

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessionsWithReadings(): Flow<List<SessionWithReadings>>

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
}
