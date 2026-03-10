package com.mdtuhinhasnat.smartwheelchairdatacollector.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mdtuhinhasnat.smartwheelchairdatacollector.bluetooth.BluetoothDeviceDomain
import com.mdtuhinhasnat.smartwheelchairdatacollector.bluetooth.ConnectionState
import com.mdtuhinhasnat.smartwheelchairdatacollector.data.SensorDataRepository
import com.mdtuhinhasnat.smartwheelchairdatacollector.data.SessionWithReadings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import android.util.Log

class MainViewModel(private val repository: SensorDataRepository) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = repository.bluetoothService.connectionState
    val incomingData: SharedFlow<String> = repository.bluetoothService.incomingData

    val allSessions: StateFlow<List<SessionWithReadings>> = repository.getAllSessionsWithReadings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            incomingData.collect { jsonString ->
                if (jsonString != null) {
                    repository.saveJsonReadingsToDatabase(jsonString)
                }
            }
        }
    }

    fun getPairedDevices(): List<BluetoothDeviceDomain> {
        return repository.bluetoothService.getPairedDevices()
    }

    fun connectToDevice(macAddress: String) {
        viewModelScope.launch {
            repository.bluetoothService.connectToDevice(macAddress)
        }
    }

    fun disconnect() {
        repository.bluetoothService.disconnect()
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            repository.deleteAllSessions()
        }
    }

    fun exportToCsv(context: Context, uri: Uri, sessions: List<SessionWithReadings>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                    val writer = OutputStreamWriter(outputStream)
                    
                    // Generate Header
                    val header = StringBuilder("id,timestamp")
                    for (i in 0 until 120) {
                        header.append(",ax_$i,ay_$i,az_$i,gx_$i,gy_$i,gz_$i,w_$i")
                    }
                    header.append("\n")
                    writer.write(header.toString())

                    // Generate Rows
                    for (sessionData in sessions) {
                        val row = StringBuilder("${sessionData.session.sessionId},${sessionData.session.timestamp}")
                        
                        // Sort by index to ensure correct order
                        val sortedReadings = sessionData.readings.sortedBy { it.sampleIndex }
                        
                        for (reading in sortedReadings) {
                            row.append(",${reading.ax},${reading.ay},${reading.az},${reading.gx},${reading.gy},${reading.gz},${reading.weight}")
                        }
                        
                        // Fill missing columns if for some reason reading count is < 120 (defensive programming)
                        for (i in sortedReadings.size until 120) {
                            row.append(",0.0,0.0,0.0,0.0,0.0,0.0,0.0")
                        }
                        row.append("\n")
                        writer.write(row.toString())
                    }
                    
                    writer.flush()
                    writer.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class MainViewModelFactory(private val repository: SensorDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
