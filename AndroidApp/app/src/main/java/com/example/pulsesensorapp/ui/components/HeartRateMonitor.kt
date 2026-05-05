package com.example.pulsesensorapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pulsesensorapp.ui.theme.PulseSensorAppTheme

@Composable
fun HeartRateMonitor(
    connected: Boolean, heartRate: Int, oxygen: Int, modifier: Modifier = Modifier
) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeartRateCard(heartRate)
            SpO2Card(spo2 = oxygen)
        }

    Box(modifier = Modifier.fillMaxSize()) {
        SensorStatusIcon(
            connected = connected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false
)
@Composable
fun HeartRateMonitorPreview() {
    PulseSensorAppTheme {
        HeartRateMonitor(
            connected = false,
            heartRate = 60,
            oxygen = 80,
        )
    }
}