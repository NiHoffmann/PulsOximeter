package com.example.pulsesensorapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pulsesensorapp.R

@Composable
fun SensorStatusIcon(
    connected: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, icon) = if (connected) {
        MaterialTheme.colorScheme.primary to R.drawable.baseline_bluetooth_connected_24
    } else {
        MaterialTheme.colorScheme.error to R.drawable.baseline_bluetooth_searching_24
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = color.copy(alpha = 0.15f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "Sensor status",
            tint = color,
            modifier = Modifier.size(28.dp)
        )
    }
}