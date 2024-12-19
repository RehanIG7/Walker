package com.example.nativesteptracker

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.nativesteptracker.ui.screens.HomeScreen
import com.example.nativesteptracker.ui.theme.NativeStepTrackerTheme
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient

class MainActivity : ComponentActivity(), SensorEventListener {

    private val tag = "StepCounter"
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps by mutableStateOf(0f)
    private var totalDistance by mutableStateOf(0f)

    private val STEP_LENGTH_METERS = 0.75f // Example: 0.75 meters per step
    private val MIN_STEP_INTERVAL = 500 // 500 ms between steps for valid detection
    private val MIN_GPS_SPEED_KMH = 3.0f // Minimum speed (km/h) considered as walking speed

    private var lastStepTime: Long = 0
    private var lastGpsSpeed: Float = 0.0f // in km/h

    // FusedLocationProviderClient for GPS updates
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Permission launcher for Location and Activity Recognition
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] == true) {
                // Permissions granted, initialize location and sensors
                initializeLocationUpdates()
//                initializeStepCounter()
            } else {
                Toast.makeText(this, "Permissions denied. Some features may not work.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for permissions
        checkAndRequestPermissions()

        setContent {
            HomeScreen(steps = totalSteps, totalDistance = totalDistance)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACTIVITY_RECOGNITION)

        // Check if permissions are already granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            // Request missing permissions
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        } else {
            initializeLocationUpdates()
            initializeStepCounter()
        }
    }





    private fun initializeLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .build()

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates using the fusedLocationClient
            fusedLocationClient.requestLocationUpdates(locationRequest,  object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    p0.let {
                        // Loop through all locations in the result (usually there will be only one)
                        it.locations.forEach { location ->
                            // Log the current location's latitude and longitude
                            Log.d(tag, "Current Location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")

                            // Update GPS speed as well (convert from m/s to km/h)
                            val speedInKmh = location.speed * 3.6f // Convert speed from m/s to km/h
                            lastGpsSpeed = speedInKmh
                            Log.d(tag, "Speed in km/h: $speedInKmh")
                        }
                    }
                }
            }, null)
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }


    }

    // Location callback for receiving location updates
    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult?) {
//            if (locationResult != null) {
//                super.onLocationResult(locationResult)
//            }
//
//            // Check if locationResult is null, and handle accordingly
//            locationResult?.let { result ->
//                // Safely access the locations from LocationResult
//                result.locations.forEach { location ->
//                    // Log the coordinates
//                    Log.d(tag, "Current Location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
//
//                    // Update GPS speed
//                    val speedInKmh = location.speed * 3.6f // Speed in km/h
//                    lastGpsSpeed = speedInKmh
//                }
//            } ?: run {
//                // If locationResult is null, handle the case or log an error
//                Log.e(tag, "Location result is null")
//            }
//        }
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                if (running) {
                    Log.d(tag, "Step Counter: Total Steps: $totalSteps")
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (running) {
                    val currentTime = System.currentTimeMillis()

                    // Check if the device is shaking or if the user is in a vehicle
                    val isShaking = isDeviceShaking(event)  // Use event data only for shaking detection
                    val isInVehicle = lastGpsSpeed > MIN_GPS_SPEED_KMH

                    if (isShaking || isInVehicle) {
                        Log.d(tag, "False step detected due to shaking or vehicle movement")
                        return // Ignore steps if shaking or in a vehicle
                    }

                    // Calculate the interval between steps to filter out false positives
                    if (lastStepTime == 0L || currentTime - lastStepTime > MIN_STEP_INTERVAL) {
                        totalSteps += 1 // Increment step count
                        lastStepTime = currentTime
                        totalDistance = totalSteps * STEP_LENGTH_METERS
                        Log.d(tag, "Step Detector: Valid step detected. Total steps: $totalSteps, Distance: $totalDistance meters")
                    }
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                if (event.values.size >= 3) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val isShaking = isDeviceShaking(x, y, z)

                    if (isShaking) {
                        Log.d(tag, "Device shaking detected")

                        // Log the user's current GPS coordinates when the device is shaking
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            location?.let {
                                Log.d(tag, "User's Location: Latitude = ${it.latitude}, Longitude = ${it.longitude}")
                            }
                        }
                    }
                } else {
                    Log.e(tag, "Invalid accelerometer data. Expected 3 values, got ${event.values.size}.")
                }
            }

//            Sensor.TYPE_STATIONARY_DETECT -> {
//                // Handle GPS updates for location change detection (can be separate GPS update listener)
//                if (currentGpsSpeed < VEHICLE_SPEED_THRESHOLD_KMH) {
//                    // If the GPS speed is under a threshold, consider it valid walking
//                    Log.d(tag, "User is walking, valid GPS data")
//                } else {
//                    Log.d(tag, "User in vehicle, ignoring step")
//                }
//            }

            else -> {
                Log.d(tag, "Other sensor detected: ${event.sensor.type}, Values: ${event.values.joinToString()}")
            }
        }
    }

    // Function to detect if the device is shaking (false positive)
    fun isDeviceShaking(event: SensorEvent): Boolean {
        if (event.values.size < 3) {
            Log.e(tag, "Invalid accelerometer data. Expected 3 values, got ${event.values.size}.")
            return false
        }
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        return isDeviceShaking(x, y, z)
    }

    // Function to detect if the device is shaking (using x, y, z values)
    fun isDeviceShaking(x: Float, y: Float, z: Float): Boolean {
        val shakeThreshold = 12.0 // Threshold value for shaking detection
        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
        return acceleration > shakeThreshold
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
