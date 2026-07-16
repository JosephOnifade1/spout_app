package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.ui.components.GrowthIcon
import com.example.ui.components.BottomNavBar
import com.example.ui.theme.AppIcons
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    habits: List<Habit>,
    allCheckIns: List<CheckIn> = emptyList(),
    onAddHabit: () -> Unit,
    onHabitClick: (String) -> Unit,
    onCheckIn: (Habit) -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val today = LocalDate.now().toString()
    val sortedHabits = remember(habits) {
        habits.filter { !it.archived }.sortedBy { it.sortOrder }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentRoute = "today",
                onNavigate = { route -> 
                    onNavigateToTab(route) 
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = AppIcons.SproutOutline),
                            contentDescription = "Sprout icon",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                        
                        IconButton(onClick = onNotificationsClick) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text("Good morning,", style = MaterialTheme.typography.headlineLarge)
                    Text("Alex", style = MaterialTheme.typography.headlineLarge)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Your garden is growing ", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
                        )
                        Image(
                            painter = painterResource(id = AppIcons.GrowthSprout),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Habits
            items(sortedHabits) { habit ->
                val isCheckedIn = allCheckIns.any { it.habitId == habit.habitId && it.date == today }
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                    HabitCard(
                        habit = habit, 
                        isCheckedIn = isCheckedIn,
                        onClick = { onHabitClick(habit.habitId) }, 
                        onCheckIn = { onCheckIn(habit) }
                    )
                }
            }
            
            // Add Habit Button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onAddHabit,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add habit", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun HabitCard(habit: Habit, isCheckedIn: Boolean, onClick: () -> Unit, onCheckIn: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCheckedIn) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp, 
            if (isCheckedIn) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // Left Icon
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
                    colorFilter = ColorFilter.tint(
                        if (isCheckedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.size(44.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Middle Texts
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.title, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCheckedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isCheckedIn) "Goal achieved!" else "Today's target" + (habit.targetText?.let { ": $it" } ?: ""), 
                        style = MaterialTheme.typography.bodySmall, 
                        color = if (isCheckedIn) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
                    )
                }
            }
            
            // Right Check-in Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCheckIn() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isCheckedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCheckedIn) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked In",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    } else {
                        Image(
                            painter = painterResource(id = AppIcons.GrowthBud),
                            contentDescription = "Check In Icon",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (isCheckedIn) "Checked in ✓" else "Check in", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = if (isCheckedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }
}
