package com.mdtuhinhasnat.smartwheelchairdatacollector.data

import android.content.Context
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
            if (readings.size == 120) {
                dao.insertSessionWithReadings(session, readings)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSessionById(sessionId)
    }

    suspend fun deleteAllSessions() {
        dao.deleteAllSessions()
    }
}
