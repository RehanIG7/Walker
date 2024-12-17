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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nativesteptracker.ui.theme.NativeStepTrackerTheme

class MainActivity : ComponentActivity(), SensorEventListener {
    private val tag = "StepCounter"

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var steps = 0

    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    // ActivityResultLauncher for requesting Activity Recognition Permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(tag, "Permission granted")
                startStepTracking()
            } else {
                Log.d(tag, "Permission denied")
                showPermissionDeniedMessage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Check and request permission
        requestActivityRecognitionPermission()

        setContent {
            NativeStepTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                            .padding(20.dp, 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Step Counter",
                            modifier = Modifier.padding(0.dp, 100.dp, 0.dp, 0.dp),
                            fontSize = 25.sp,
                            color = Color.Black,
                            fontStyle = FontStyle.Italic
                        )

                        Text(
                            text = "Steps : $steps",
                            modifier = Modifier.padding(0.dp, 100.dp, 0.dp, 0.dp),
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontStyle = FontStyle.Normal
                        )

                        Row(
                            modifier = Modifier.padding(0.dp, 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Button(onClick = {
                                Log.d(tag, "start button clicked")
                                startStepTracking()
                            }) {
                                Text("Start")
                            }
                            Button(onClick = {
                                Log.d(tag, "pause button clicked")
                                sensorManager.unregisterListener(this@MainActivity)
                            }) {
                                Text("Pause")
                            }
                        }

                        Button(onClick = {
                            Log.d(tag, "sensorManager: $sensorManager")
                            Log.d(tag, "stepSensor: $stepSensor")
                        }) {
                            Text("Display value")
                        }
                    }
                }
            }
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+ requires this permission
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    startStepTracking()
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        } else {
            // For devices below Android 10, permission is not needed
            startStepTracking()
        }
    }

    private fun startStepTracking() {
        // Register the step sensor listener
        stepSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            Log.d(tag, "Step tracking started...")
        } ?: Log.d(tag, "Step Sensor not available")
    }

    private fun showPermissionDeniedMessage() {
        Log.d(tag, "Permission denied! Cannot track steps.")
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            stepSensor?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister listener to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val currentSteps = event.values[0] - previousTotalSteps
            steps = currentSteps.toInt()
            Log.d(tag, "Total Steps: $steps")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for TYPE_STEP_COUNTER
    }
}
