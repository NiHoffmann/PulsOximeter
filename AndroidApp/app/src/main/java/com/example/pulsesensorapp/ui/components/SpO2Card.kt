package com.example.pulsesensorapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SpO2Card(
    spo2: Int,
    modifier: Modifier = Modifier
) {
    val value = spo2.coerceIn(0, 100)

    val progressColor = when {
        value >= 95 -> MaterialTheme.colorScheme.primary
        value >= 90 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = modifier
            .size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { value / 100f },
            strokeWidth = 10.dp,
            color = progressColor,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$value%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Text(
                text = "SpO₂",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}