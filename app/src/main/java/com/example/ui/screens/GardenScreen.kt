package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CheckIn
import com.example.data.GrowthStatus
import com.example.data.Habit
import com.example.ui.components.BottomNavBar
import com.example.ui.components.GrowthIcon
import com.example.ui.theme.AppIcons
import com.example.ui.theme.SproutTypography
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class GardenSortOption {
    MANUAL, CONSISTENT, GROWTH, NEWEST
}

@Composable
fun SlidersIcon(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val circleRadius = 3.dp.toPx()
        
        // Line 1
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.25f), end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.25f), strokeWidth = strokeWidth)
        drawCircle(color, radius = circleRadius, center = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.25f))
        
        // Line 2
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.5f), end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.5f), strokeWidth = strokeWidth)
        drawCircle(color, radius = circleRadius, center = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.5f))
        
        // Line 3
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.75f), end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.75f), strokeWidth = strokeWidth)
        drawCircle(color, radius = circleRadius, center = androidx.compose.ui.geometry.Offset(size.width * 0.6f, size.height * 0.75f))
    }
}

@Composable
fun SoilBedMound(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, size.height)
            cubicTo(
                size.width * 0.2f, size.height * 0.2f,
                size.width * 0.8f, size.height * 0.2f,
                size.width, size.height
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color = Color(0xFF8C7A6B)) // Earthy brown
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    habits: List<Habit>,
    allCheckIns: List<CheckIn>,
    onHabitClick: (String) -> Unit,
    onReorderHabits: (List<String>) -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    var sortOption by remember { mutableStateOf(GardenSortOption.MANUAL) }
    var showSortMenu by remember { mutableStateOf(false) }

    val activeHabits = habits.filter { !it.archived }
    
    val sortedHabits = remember(activeHabits, sortOption) {
        when (sortOption) {
            GardenSortOption.MANUAL -> activeHabits.sortedBy { it.sortOrder }
            GardenSortOption.CONSISTENT -> activeHabits.sortedByDescending { habit ->
                allCheckIns.filter { it.habitId == habit.habitId }.map { it.date }.distinct().size
            }
            GardenSortOption.GROWTH -> activeHabits.sortedByDescending { it.growthPoints }
            GardenSortOption.NEWEST -> activeHabits.sortedByDescending { it.createdAt }
        }
    }

    // Local list for reordering to avoid jumping while dragging
    var localHabitList by remember(sortedHabits) { mutableStateOf(sortedHabits) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyColumnState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
        // Adjust indices because of header items (Header: 0, Bed: 1, Summary: 2)
        val headerOffset = 3
        val fromIndex = from.index - headerOffset
        val toIndex = to.index - headerOffset
        
        if (fromIndex in localHabitList.indices && toIndex in localHabitList.indices) {
            val newList = localHabitList.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            localHabitList = newList
            onReorderHabits(newList.map { it.habitId })
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentRoute = "garden",
                onNavigate = onNavigateToTab
            )
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Garden",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    OutlinedIconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        SlidersIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }

            // Wide soil-bed illustration
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(200.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Soil bed mound
                    SoilBedMound(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    )
                    
                    // Plants growing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp, start = 32.dp, end = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        activeHabits.take(5).forEach { habit ->
                            val status = when {
                                habit.growthPoints < 5 -> GrowthStatus.SEED
                                habit.growthPoints < 15 -> GrowthStatus.SPROUT
                                habit.growthPoints < 30 -> GrowthStatus.BUD
                                else -> GrowthStatus.BLOOM
                            }
                            GrowthIcon(status = status, modifier = Modifier.size(90.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Weekly Summary Card
            item {
                WeeklySummaryCard(allCheckIns)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Habit list
            items(localHabitList, key = { it.habitId }) { habit ->
                ReorderableItem(reorderableLazyColumnState, key = habit.habitId) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    val strongest = remember(localHabitList, allCheckIns) { 
                        findStrongestHabit(localHabitList, allCheckIns) 
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .shadow(elevation, RoundedCornerShape(20.dp))
                    ) {
                        GardenHabitCard(
                            habit = habit,
                            weeklyCount = allCheckIns.filter { it.habitId == habit.habitId }.map { it.date }.distinct().size,
                            isStrongest = habit == strongest,
                            onClick = { onHabitClick(habit.habitId) },
                            reorderModifier = Modifier.draggableHandle()
                        )
                    }
                }
            }

            // Add Habit Button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add habit", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showSortMenu) {
        ModalBottomSheet(onDismissRequest = { showSortMenu = false }) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Sort Habits", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                GardenSortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                sortOption = option
                                showSortMenu = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sortOption == option, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when(option) {
                                GardenSortOption.MANUAL -> "Manual order"
                                GardenSortOption.CONSISTENT -> "Most consistent this week"
                                GardenSortOption.GROWTH -> "Most total growth"
                                GardenSortOption.NEWEST -> "Newest first"
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(allCheckIns: List<CheckIn>) {
    // Simplified calculation for demo
    val distinctDays = allCheckIns.map { it.date }.distinct().size
    val totalDays = 7 

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = AppIcons.GrowthSprout),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "You showed up ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$distinctDays",
                        style = SproutTypography.TechnicalMono
                    )
                    Text(
                        text = " of ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$totalDays",
                        style = SproutTypography.TechnicalMono
                    )
                    Text(
                        text = " days",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = "this week. Beautiful consistency.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun GardenHabitCard(
    habit: Habit,
    weeklyCount: Int,
    isStrongest: Boolean,
    onClick: () -> Unit,
    reorderModifier: Modifier
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag Handle
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Reorder",
                    modifier = reorderModifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Habit Icon
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)),
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.title, 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "$weeklyCount/7 this week", 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
                // Consistency stat large
                Text(
                    "$weeklyCount / 7",
                    style = SproutTypography.TechnicalMono.copy(fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
        
        if (isStrongest) {
            Surface(
                modifier = Modifier.padding(top = 0.dp, end = 40.dp).offset(y = (-10).dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = AppIcons.GrowthSprout),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Growing strongest this week",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun findStrongestHabit(habits: List<Habit>, allCheckIns: List<CheckIn>): Habit? {
    if (habits.isEmpty()) return null
    val weeklyCounts = habits.associateWith { habit ->
        allCheckIns.count { it.habitId == habit.habitId }
    }
    val maxCount = weeklyCounts.values.maxOrNull() ?: 0
    if (maxCount == 0) return null
    
    val tiedHabits = weeklyCounts.filter { it.value == maxCount }.keys.toList()
    if (tiedHabits.size == 1) return tiedHabits[0]
    
    // Tiebreaker: Most growth points
    return tiedHabits.maxByOrNull { it.growthPoints }
}
