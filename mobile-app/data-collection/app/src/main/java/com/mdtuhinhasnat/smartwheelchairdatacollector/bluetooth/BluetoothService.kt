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
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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

    private val _incomingData = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val incomingData: SharedFlow<String> = _incomingData

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
                Log.d("BluetoothService", "Socket connected! Starting listener...")
                _connectionState.value = ConnectionState.CONNECTED
                startListening(socket!!.inputStream)
            } else {
                Log.e("BluetoothService", "Socket connect returned true but isConnected is false")
                _connectionState.value = ConnectionState.ERROR
            }
        } catch (e: Exception) {
            Log.e("BluetoothService", "Connection Failed: ${e.message}", e)
            socket?.close()
            _connectionState.value = ConnectionState.ERROR
        }
    }

    private suspend fun startListening(inputStream: InputStream) = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        Log.d("BluetoothService", "Listening loop started")
        try {
            while (socket?.isConnected == true) {
                // The Arduino sends a large JSON array ending with println (newline)
                val line = reader.readLine()
                if (line != null) {
                    Log.d("BluetoothService", "Received line length: ${line.length}")
                    if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                        Log.d("BluetoothService", "Valid JSON array detected")
                        _incomingData.tryEmit(line)
                    } else {
                        Log.w("BluetoothService", "Line received but did not match expected JSON array start/end")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BluetoothService", "Listening Failed: ${e.message}", e)
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
