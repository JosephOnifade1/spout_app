package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.ui.components.GrowthBedRow
import com.example.ui.theme.AppIcons
import com.example.ui.theme.PerennialTypography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit,
    recentCheckIns: List<CheckIn>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onArchive: (Habit) -> Unit,
    onUnarchive: (Habit) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Text("⋮", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(habit.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Keep nourishing your mind.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Big icon using AppIcons
            val habitIconRes = when(habit.iconKey) {
                "water" -> AppIcons.HabitWater
                "read" -> AppIcons.HabitRead
                "exercise" -> AppIcons.HabitExercise
                "meditate" -> AppIcons.HabitMeditate
                else -> AppIcons.HabitWater
            }
            Image(
                painter = painterResource(id = habitIconRes),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Your Growth Bed \uD83C\uDF3F", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Derive statuses for last 7 days according to new rules
            val statuses = remember(habit, recentCheckIns) {
                val today = LocalDate.now()
                val createdAt = Instant.ofEpochMilli(habit.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                
                (0..6).reversed().map { dayOffset ->
                    val date = today.minusDays(dayOffset.toLong())
                    val dateStr = date.toString()
                    val checkIn = recentCheckIns.find { it.date == dateStr }
                    
                    when {
                        date.isBefore(createdAt) -> GrowthStatus.BLANK
                        !isScheduled(habit, date) -> GrowthStatus.UNSCHEDULED
                        checkIn == null -> GrowthStatus.RESTING
                        else -> {
                            try {
                                GrowthStatus.valueOf(checkIn.status.uppercase())
                            } catch (e: Exception) {
                                GrowthStatus.SEED
                            }
                        }
                    }
                }
            }
            
            GrowthBedRow(recentStatuses = statuses)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current streak", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = habit.currentStreak.toString().padStart(3, '0'),
                        style = PerennialTypography.TechnicalMono.copy(fontSize = 45.sp),
                        fontWeight = FontWeight.Medium
                    )
                    Text("days", style = MaterialTheme.typography.labelMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current stage", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentStatus = when {
                        habit.currentStreak >= 7 -> GrowthStatus.BLOOM
                        habit.currentStreak >= 4 -> GrowthStatus.BUD
                        habit.currentStreak >= 2 -> GrowthStatus.SPROUT
                        habit.currentStreak >= 1 -> GrowthStatus.SEED
                        else -> GrowthStatus.RESTING
                    }
                    Text(
                        text = currentStatus.value.uppercase(),
                        style = PerennialTypography.TechnicalMono.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Text("nourishing", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onEdit, 
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Moss green
                        contentColor = MaterialTheme.colorScheme.onPrimary // White
                    )
                ) {
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = { 
                        if (habit.archived) {
                            onUnarchive(habit)
                        } else {
                            onArchive(habit)
                        }
                    }, 
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Text(if (habit.archived) "Unarchive" else "Archive")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun isScheduled(habit: Habit, date: LocalDate): Boolean {
    return when (habit.frequency.lowercase()) {
        "daily" -> true
        "weekdays" -> date.dayOfWeek.value in 1..5
        "custom" -> habit.customDays.contains(date.dayOfWeek.value)
        else -> true
    }
}
