package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.ui.components.GrowthBedRow
import com.example.ui.theme.IbmPlexMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(habit.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Keep nourishing your mind.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Big icon placeholder
            Text("\uD83D\uDCD6", style = MaterialTheme.typography.displayLarge)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Your Growth Bed \uD83C\uDF3F", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            val mockStatuses = listOf(
                GrowthStatus.SEED, GrowthStatus.SPROUT, GrowthStatus.SPROUT,
                GrowthStatus.BUD, GrowthStatus.RESTING, GrowthStatus.SEED, GrowthStatus.BLOOM
            )
            
            GrowthBedRow(recentStatuses = mockStatuses)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current streak", style = MaterialTheme.typography.labelLarge)
                    Text(habit.currentStreak.toString().padStart(3, '0'), style = MaterialTheme.typography.displayMedium.copy(fontFamily = IbmPlexMonoFontFamily), fontWeight = FontWeight.Bold)
                    Text("days", style = MaterialTheme.typography.labelMedium)
                }
                HorizontalDivider(modifier = Modifier.height(60.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Longest streak", style = MaterialTheme.typography.labelLarge)
                    Text(habit.longestStreak.toString().padStart(3, '0'), style = MaterialTheme.typography.displayMedium.copy(fontFamily = IbmPlexMonoFontFamily), fontWeight = FontWeight.Bold)
                    Text("days", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("Archive")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
