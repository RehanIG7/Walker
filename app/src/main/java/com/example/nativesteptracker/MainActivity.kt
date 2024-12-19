package com.example.nativesteptracker

import android.Manifest
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
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.nativesteptracker.ui.screens.HomeScreen

class MainActivity : ComponentActivity(), SensorEventListener {

    private val tag = "StepCounter"
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps by mutableStateOf(0f)
    private var totalStepByStepCounter by mutableStateOf(0f)
    private var totalDistance by mutableStateOf(0f)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initializeStepCounter()
            } else {
                Toast.makeText(this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if permission is required for this Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkAndRequestPermission()
        } else {
            initializeStepCounter() // No permission required below Android Q
        }

        setContent {
            HomeScreen(
                steps = totalSteps,
                totalDistance = totalDistance,
                totalStepByStepCounter = totalStepByStepCounter
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
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
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                totalSteps += 1
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
