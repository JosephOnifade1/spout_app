package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.GrowthStatus

@Composable
fun GrowthBedRow(recentStatuses: List<GrowthStatus>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayStatuses = if (recentStatuses.size > 7) recentStatuses.takeLast(7) else recentStatuses
        displayStatuses.forEach { status ->
            GrowthIcon(status = status)
        }
    }
}
