package com.example.nativesteptracker

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nativesteptracker.ui.screens.HomeScreen
import com.example.nativesteptracker.ui.theme.NativeStepTrackerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity(), SensorEventListener {
    private val tag = "StepCounter"
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps by mutableStateOf(0f)
    private val MIN_STEP_INTERVAL = 500 // Minimum time between steps in milliseconds
    private var lastStepTime: Long = 0

    // Permission launcher for Activity Recognition
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(tag, "Activity Recognition permission granted")
                initializeStepCounter()
            } else {
                Toast.makeText(this, "Permission denied. Step tracking won't work.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for permissions
        checkAndRequestPermissions()

        setContent {
            HomeScreen(steps = totalSteps)
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+ requires ACTIVITY_RECOGNITION
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                initializeStepCounter()
            }
        } else {
            initializeStepCounter()
        }
    }

    private fun initializeStepCounter() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val stepCounter = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val stepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepCounter != null) {
            sensorManager?.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
            Log.d(tag, "Step Counter sensor registered")
        } else {
            Toast.makeText(this, "Step Counter sensor not available", Toast.LENGTH_SHORT).show()
        }

        if (stepDetector != null) {
            sensorManager?.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI)
            Log.d(tag, "Step Detector sensor registered")
        } else {
            Toast.makeText(this, "Step Detector sensor not available", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sensor detected on this device", Toast.LENGTH_SHORT).show()
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener to save battery
        sensorManager?.unregisterListener(this)
    }

//    override fun onSensorChanged(event: SensorEvent?) {
//        if (running) {
////            Log.d(tag,"${event!!.values}")
//            event?.let {
//                try {
//                    // Use reflection to log all fields of the SensorEvent
//                    val fields = it::class.java.declaredFields
//                    Log.d(tag, "SensorEvent Details:")
//                    fields.forEach { field ->
//                        field.isAccessible = true // Make private fields accessible
//                        Log.d(tag, "${field.name}: ${field.get(it)}")
//                    }
//                } catch (e: Exception) {
//                    Log.e(tag, "Error logging SensorEvent fields: ${e.message}")
//                }
//            } ?: Log.e(tag, "Sensor Event is null")
//
//            totalSteps = event!!.values[0]
//            Log.d(tag, "Total Steps: $totalSteps")
//        }
//    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    if (running) {
                        // Handle cumulative step count here if needed
                        Log.d(tag, "Step Counter: Total Steps: ${event.values[0]}")
                    }
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    if (running) {
                        // Filter false positives using time interval
                        val currentTime = System.currentTimeMillis()
                        if (lastStepTime == 0L || currentTime - lastStepTime > MIN_STEP_INTERVAL) {
                            totalSteps += 1 // Increment step count for each step detected
                            lastStepTime = currentTime
                            Log.d(tag, "Step Detector: Valid step detected. Total steps: $totalSteps")
                        } else {
                            Log.d(tag, "Step Detector: Ignored false positive due to rapid events")
                        }
                    }
                }
            }
        } else {
            Log.e(tag, "Sensor Event is null")
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for TYPE_STEP_COUNTER
    }
}
