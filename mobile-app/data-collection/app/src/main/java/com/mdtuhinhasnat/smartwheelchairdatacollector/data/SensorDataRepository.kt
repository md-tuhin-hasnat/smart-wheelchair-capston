package com.mdtuhinhasnat.smartwheelchairdatacollector.data

import android.content.Context
import android.util.Log
import com.mdtuhinhasnat.smartwheelchairdatacollector.bluetooth.BluetoothService
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class SensorDataRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.sensorDataDao()
    
    val bluetoothService = BluetoothService(context)

    fun getAllSessionsWithReadings(): Flow<List<SessionWithReadings>> {
        return dao.getAllSessionsWithReadings()
    }

    suspend fun saveJsonReadingsToDatabase(jsonString: String) {
        try {
            val jsonArray = JSONArray(jsonString)
            val session = SessionEntity(timestamp = System.currentTimeMillis())
            val readings = mutableListOf<SensorReadingEntity>()
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                readings.add(
                    SensorReadingEntity(
                        sessionId = 0, // Will be replaced by DAO
                        sampleIndex = i,
                        ax = item.getDouble("ax").toFloat(),
                        ay = item.getDouble("ay").toFloat(),
                        az = item.getDouble("az").toFloat(),
                        gx = item.getDouble("gx").toFloat(),
                        gy = item.getDouble("gy").toFloat(),
                        gz = item.getDouble("gz").toFloat(),
                        weight = item.getDouble("w").toFloat()
                    )
                )
            }
            
            // Only save if we got exactly 120 samples as expected
            Log.d("SensorDataRepository", "Parsed ${readings.size} readings")
            if (readings.size == 120) {
                dao.insertSessionWithReadings(session, readings)
                Log.d("SensorDataRepository", "Successfully inserted session to DB")
            } else {
                Log.e("SensorDataRepository", "Expected 120 readings, got ${readings.size}. Discarding.")
            }
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Error parsing JSON or DB Insert: ${e.message}", e)
        }
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSessionById(sessionId)
    }

    suspend fun deleteAllSessions() {
        dao.deleteAllSessions()
    }
}
