package com.mdtuhinhasnat.smartwheelchairdatacollector.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mdtuhinhasnat.smartwheelchairdatacollector.bluetooth.BluetoothDeviceDomain
import com.mdtuhinhasnat.smartwheelchairdatacollector.bluetooth.ConnectionState
import com.mdtuhinhasnat.smartwheelchairdatacollector.data.SessionWithReadings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val incomingData by viewModel.incomingData.collectAsState()

    var selectedDevice by remember { mutableStateOf<BluetoothDeviceDomain?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Permissions logic
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        permissions.add(Manifest.permission.BLUETOOTH)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Export CSV launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToCsv(context, uri, allSessions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Wheelchair Collector", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        exportLauncher.launch("wheelchair_data_$timestamp.csv")
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = allSessions.isNotEmpty()
                ) {
                    Text("Export All to CSV", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.deleteAllSessions() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Bluetooth connection card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bluetooth Connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Device Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDevice?.name ?: selectedDevice?.macAddress ?: "Select Device",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select Device")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            val devices = viewModel.getPairedDevices()
                            if (devices.isEmpty()) {
                                DropdownMenuItem(text = { Text("No paired devices found") }, onClick = { expanded = false })
                            } else {
                                devices.forEach { device ->
                                    DropdownMenuItem(
                                        text = { Text(device.name ?: device.macAddress) },
                                        onClick = {
                                            selectedDevice = device
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        // Invisible clickable overlay to trigger dropdown
                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable { expanded = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = when (connectionState) {
                            ConnectionState.CONNECTED -> Color(0xFF31A24C)
                            ConnectionState.ERROR -> MaterialTheme.colorScheme.error
                            ConnectionState.CONNECTING -> Color(0xFFE4A11B)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = "Status: ${connectionState.name}",
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (connectionState == ConnectionState.CONNECTED) {
                            Button(onClick = { viewModel.disconnect() }, shape = RoundedCornerShape(8.dp)) {
                                Text("Disconnect")
                            }
                        } else {
                            Button(
                                onClick = { selectedDevice?.let { viewModel.connectToDevice(it.macAddress) } },
                                shape = RoundedCornerShape(8.dp),
                                enabled = selectedDevice != null && connectionState != ConnectionState.CONNECTING
                            ) {
                                Text(if (connectionState == ConnectionState.CONNECTING) "Connecting..." else "Connect")
                            }
                        }
                    }

                    if (incomingData != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Receiving data...", color = Color(0xFF31A24C), fontSize = 12.sp)
                    }
                }
            }

            // Sessions List
            Text(
                text = "Collected Data (${allSessions.size})",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allSessions, key = { it.session.sessionId }) { sessionData ->
                    SessionItem(sessionData = sessionData, onDelete = { viewModel.deleteSession(it) })
                }
            }
        }
    }
}

@Composable
fun SessionItem(sessionData: SessionWithReadings, onDelete: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Session #${sessionData.session.sessionId}", fontWeight = FontWeight.Bold)
                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                Text(sdf.format(Date(sessionData.session.timestamp)), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${sessionData.readings.size} samples recorded", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onDelete(sessionData.session.sessionId) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
