package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.Habit
import com.example.ui.theme.PerennialTypography
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTimesScreen(
    habits: List<Habit>,
    onBack: () -> Unit,
    onUpdateTime: (Habit, String) -> Unit
) {
    var activeEditingHabit by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Notification Times", style = PerennialTypography.HeadlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active habits found", style = PerennialTypography.BodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    Text(
                        "Set your preferred check-in time for each habit.",
                        style = PerennialTypography.BodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
                
                items(habits) { habit ->
                    NotificationTimeRow(
                        habit = habit,
                        onTimeClick = { activeEditingHabit = habit }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }

    activeEditingHabit?.let { habit ->
        TimeSelectionDialog(
            initialTime = habit.reminderTime,
            onDismiss = { activeEditingHabit = null },
            onConfirm = { selected ->
                onUpdateTime(habit, selected)
                activeEditingHabit = null
            }
        )
    }
}

@Composable
private fun TimeSelectionDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initialTime.split(" ", ":")
    var hour by remember { mutableStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 7) }
    var minute by remember { mutableStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 30) }
    var amPm by remember { mutableStateOf(parts.getOrNull(2) ?: "PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Reminder Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { hour = if (hour == 12) 1 else hour + 1 }) {
                            Text("▲", color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = hour.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { hour = if (hour == 1) 12 else hour - 1 }) {
                            Text("▼", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(":", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { minute = (minute + 5) % 60 }) {
                            Text("▲", color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = minute.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { minute = if (minute == 0) 55 else (minute - 5 + 60) % 60 }) {
                            Text("▼", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { amPm = if (amPm == "AM") "PM" else "AM" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(amPm)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val formattedTime = String.format("%02d:%02d %s", hour, minute, amPm)
                onConfirm(formattedTime)
            }) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationTimeRow(habit: Habit, onTimeClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTimeClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(habit.title, style = PerennialTypography.BodyLarge)
            Text(
                if (habit.reminderEnabled) "Reminders enabled" else "Reminders disabled",
                style = PerennialTypography.LabelSmallMono,
                color = if (habit.reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Schedule, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    habit.reminderTime,
                    style = PerennialTypography.TechnicalMono,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
