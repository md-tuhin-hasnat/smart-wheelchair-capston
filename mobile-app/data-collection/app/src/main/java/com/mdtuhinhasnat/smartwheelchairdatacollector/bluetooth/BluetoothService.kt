package com.mdtuhinhasnat.smartwheelchairdatacollector.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

data class BluetoothDeviceDomain(
    val name: String?,
    val macAddress: String
)

@SuppressLint("MissingPermission")
class BluetoothService(private val context: Context) {

    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var socket: BluetoothSocket? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _incomingData = MutableStateFlow<String?>(null)
    val incomingData: StateFlow<String?> = _incomingData

    companion object {
        // Standard SPP UUID
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    fun getPairedDevices(): List<BluetoothDeviceDomain> {
        if (bluetoothAdapter == null) return emptyList()
        return bluetoothAdapter.bondedDevices.map {
            BluetoothDeviceDomain(it.name, it.address)
        }
    }

    suspend fun connectToDevice(macAddress: String) = withContext(Dispatchers.IO) {
        if (bluetoothAdapter == null) return@withContext
        try {
            _connectionState.value = ConnectionState.CONNECTING
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            
            // Cancel discovery as it slows down the connection
            bluetoothAdapter.cancelDiscovery()

            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket?.connect()

            if (socket?.isConnected == true) {
                _connectionState.value = ConnectionState.CONNECTED
                startListening(socket!!.inputStream)
            } else {
                _connectionState.value = ConnectionState.ERROR
            }
        } catch (e: Exception) {
            e.printStackTrace()
            socket?.close()
            _connectionState.value = ConnectionState.ERROR
        }
    }

    private suspend fun startListening(inputStream: InputStream) = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            while (socket?.isConnected == true) {
                // The Arduino sends a large JSON array ending with println (newline)
                val line = reader.readLine()
                if (line != null && line.trim().startsWith("[") && line.trim().endsWith("]")) {
                    _incomingData.value = line
                    // Clear it immediately to allow subsequent identical string emissions if needed
                    _incomingData.value = null 
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = ConnectionState.ERROR
            socket?.close()
        }
    }

    fun disconnect() {
        try {
            socket?.close()
            socket = null
            _connectionState.value = ConnectionState.DISCONNECTED
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
