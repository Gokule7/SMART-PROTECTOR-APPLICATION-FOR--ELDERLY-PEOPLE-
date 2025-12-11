package com.raju.spawearable.presentation.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.raju.spawearable.presentation.services.SensorForegroundService

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isCheckingPermissions by remember { mutableStateOf(true) }
    var serviceStatus by remember { mutableStateOf("Checking services...") }

    // Combined permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isCheckingPermissions = false
        val bodySensorsGranted = permissions[Manifest.permission.BODY_SENSORS] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true // Default to true for older versions
        } else {
            true
        }

        when {
            bodySensorsGranted && locationGranted && notificationsGranted -> {
                serviceStatus = "Starting services..."
                SensorForegroundService.startService(context)
                serviceStatus = "Services are running"
            }
            !notificationsGranted -> {
                serviceStatus = "Notification permission needed"
                Toast.makeText(context, "Notification permission needed", Toast.LENGTH_SHORT).show()
            }
            bodySensorsGranted -> {
                serviceStatus = "Location permission needed"
                Toast.makeText(context, "Location permission needed", Toast.LENGTH_SHORT).show()
            }
            locationGranted -> {
                serviceStatus = "Body sensors permission needed"
                Toast.makeText(context, "Body sensors permission needed", Toast.LENGTH_SHORT).show()
            }
            else -> {
                serviceStatus = "Permissions denied"
                Toast.makeText(context, "Required permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-check permissions on launch
    LaunchedEffect(Unit) {
        val requiredPermissions = mutableListOf<String>().apply {
            add(Manifest.permission.BODY_SENSORS)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        val allGranted = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        when {
            allGranted -> {
                serviceStatus = "Starting services..."
                SensorForegroundService.startService(context)
                serviceStatus = "Services are running"
                isCheckingPermissions = false
            }
            requiredPermissions.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
            } -> {
                serviceStatus = "Permissions needed"
                showPermissionDialog = true
                isCheckingPermissions = false
            }
            else -> {
                serviceStatus = "Requesting permissions..."
                requestPermissions(permissionsLauncher, requiredPermissions)
                isCheckingPermissions = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(serviceStatus, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        if (!serviceStatus.contains("running", ignoreCase = true)) {
            Button(
                onClick = {
                    val requiredPermissions = mutableListOf<String>().apply {
                        add(Manifest.permission.BODY_SENSORS)
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }.toTypedArray()

                    val allGranted = requiredPermissions.all { permission ->
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }

                    when {
                        allGranted -> {
                            serviceStatus = "Starting services..."
                            SensorForegroundService.startService(context)
                            serviceStatus = "Services are running"
                        }
                        requiredPermissions.any { permission ->
                            ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
                        } -> {
                            serviceStatus = "Permissions needed"
                            showPermissionDialog = true
                        }
                        else -> {
                            serviceStatus = "Requesting permissions..."
                            requestPermissions(permissionsLauncher, requiredPermissions)
                        }
                    }
                }
            ) {
                Text("Start Monitoring")
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permissions Required") },
            text = {
                Text("""
                    This app needs:
                    1. Body Sensors - For health monitoring
                    2. Location - For activity tracking
                    3. Notifications - For important alerts
                """.trimIndent())
            },
            confirmButton = {
                Button(onClick = {
                    serviceStatus = "Requesting permissions..."
                    requestPermissions(permissionsLauncher, arrayOf(
                        Manifest.permission.BODY_SENSORS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS
                    ))
                    showPermissionDialog = false
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    serviceStatus = "Permissions required"
                }) {
                    Text("Deny")
                }
            }
        )
    }
}

private fun requestPermissions(
    launcher: ActivityResultLauncher<Array<String>>,
    permissions: Array<String> = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )
) {
    // Filter out POST_NOTIFICATIONS for devices below Android 13
    val filteredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions
    } else {
        permissions.filter { it != Manifest.permission.POST_NOTIFICATIONS }.toTypedArray()
    }

    launcher.launch(filteredPermissions)
}