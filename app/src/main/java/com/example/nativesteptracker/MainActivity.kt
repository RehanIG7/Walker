package com.example.nativesteptracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.nativesteptracker.services.StepCounterService
import com.example.nativesteptracker.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {

    private val tag = "StepCounter"
    public var totalSteps: MutableState<Float> = mutableStateOf(0f)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startStepCounterService() // Start service if permission is granted
            } else {
                Toast.makeText(this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        totalSteps.value = getSavedSteps()
        val stepCounterService = StepCounterService()

        setContent {
            HomeScreen(
                steps = totalSteps.value,
                totalDistance = 0f, // Update this as needed
                totalStepByStepCounter = 0f // Update this as needed
            )
        }

        // Check permission and start service if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkAndRequestPermission()
        } else {
            startStepCounterService() // No permission required below Android Q
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
            startStepCounterService()
        }
    }

    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun getSavedSteps(): Float {
        val sharedPreferences = getSharedPreferences("step_data", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat("total_steps", 0f)
    }
}
