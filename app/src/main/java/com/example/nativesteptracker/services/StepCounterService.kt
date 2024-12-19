package com.example.nativesteptracker.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.example.nativesteptracker.MainActivity
import com.example.nativesteptracker.R

class StepCounterService : Service(), SensorEventListener {

    private val tag = "StepCounterService"
    private var sensorManager: SensorManager? = null
    private val CHANNEL_ID = "StepCounterServiceChannel"

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        createNotificationChannel()
        startForegroundService()

        if (stepCounterSensor != null) {
            sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d(tag, "Step Counter sensor registered")
        } else {
            Log.d(tag, "Step Counter sensor not available")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
//            totalSteps.value +=1
//            Log.d(tag, "Step Counter updated: ${totalSteps.value}")

            // Save steps to SharedPreferences
//            saveSteps(totalSteps.value)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Counter Service"
            val descriptionText = "Tracks step count in the background"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter Service")
            .setContentText("Tracking steps in the background")
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with your app icon
            .build()

        startForeground(1, notification)
    }

    private fun getSavedSteps(): Float {
        val sharedPreferences = getSharedPreferences("step_data", Context.MODE_PRIVATE)
        return sharedPreferences.getFloat("total_steps", 0f)
    }

    private fun saveSteps(steps: Float) {
        val sharedPreferences = getSharedPreferences("step_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("total_steps", steps)
        editor.apply()
    }
}
