package com.example.nativesteptracker.ui.screens

import android.util.Log
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
import androidx.core.view.ContentInfoCompat.Flags
import com.example.nativesteptracker.ui.theme.NativeStepTrackerTheme

@Composable
fun HomeScreen(
    steps: Float,
    totalDistance: Float
//    onStartClick: () -> Unit,
//    onPauseClick: () -> Unit,
//    onDisplayValueClick: () -> Unit
){
    val tag = "StepCounter"
    NativeStepTrackerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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

                Text(
                    text = "Distance : $totalDistance",
                    modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp),
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
                    }) {
                        Text("Start")
                    }
                    Button(onClick = {
                        Log.d(tag, "pause button clicked")
                    }) {
                        Text("Pause")
                    }
                }

                Button(onClick = {

                }) {
                    Text("Display value")
                }
            }
        }
    }
}