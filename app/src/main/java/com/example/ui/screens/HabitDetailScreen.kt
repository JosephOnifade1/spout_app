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
import com.example.ui.theme.SproutTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit,
    recentCheckIns: List<CheckIn>,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            
            // Derive statuses from recent check-ins
            val statuses = recentCheckIns.map { 
                try {
                    GrowthStatus.valueOf(it.status.uppercase())
                } catch(e: Exception) {
                    GrowthStatus.SEED
                }
            }
            val paddedStatuses = if (statuses.isEmpty()) listOf(GrowthStatus.SEED) else statuses
            
            GrowthBedRow(recentStatuses = paddedStatuses)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total growth", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = habit.growthPoints.toString().padStart(3, '0'),
                        style = SproutTypography.TechnicalMono.copy(fontSize = 45.sp),
                        fontWeight = FontWeight.Medium
                    )
                    Text("points", style = MaterialTheme.typography.labelMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current stage", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentStatus = when {
                        habit.growthPoints < 5 -> GrowthStatus.SEED
                        habit.growthPoints < 15 -> GrowthStatus.SPROUT
                        habit.growthPoints < 30 -> GrowthStatus.BUD
                        else -> GrowthStatus.BLOOM
                    }
                    Text(
                        text = currentStatus.value.uppercase(),
                        style = SproutTypography.TechnicalMono.copy(fontSize = 20.sp),
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
                    onClick = { /* TODO Archive */ }, 
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Text("Archive")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
