package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Habit
import com.example.ui.components.PlantTag
import com.example.ui.theme.AppIcons
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    initialHabit: Habit?,
    onSave: (Habit) -> Unit,
    onBack: () -> Unit
) {
    val icons = listOf(
        Pair("water", AppIcons.HabitWater),
        Pair("read", AppIcons.HabitRead),
        Pair("exercise", AppIcons.HabitExercise),
        Pair("meditate", AppIcons.HabitMeditate),
        Pair("sprout", AppIcons.HabitSprout),
        Pair("pot", AppIcons.HabitPot),
        Pair("bud", AppIcons.HabitBud),
        Pair("sun", AppIcons.HabitSun)
    )

    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var selectedIconKey by remember { mutableStateOf(initialHabit?.iconKey ?: "water") }
    var selectedColor by remember { mutableStateOf(initialHabit?.colorTag ?: "moss") }
    var frequency by remember { mutableStateOf(initialHabit?.frequency?.lowercase() ?: "daily") }
    var selectedCustomDays by remember { mutableStateOf(initialHabit?.customDays ?: emptyList<Int>()) }
    var reminderEnabled by remember { mutableStateOf(initialHabit?.reminderEnabled ?: true) }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "07:30 PM") }
    var showTimePicker by remember { mutableStateOf(false) }

    val colors = listOf("moss", "dustyClay", "butter", "soil", "inkTeal", "charcoalInk")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val context = androidx.compose.ui.platform.LocalContext.current
            TopAppBar(
                title = { Text(if (initialHabit == null) "Add Habit" else "Edit Habit", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        if (title.isBlank()) {
                            android.widget.Toast.makeText(context, "Please enter a habit title", android.widget.Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        if (frequency == "custom" && selectedCustomDays.isEmpty()) {
                            android.widget.Toast.makeText(context, "Please select at least one custom day", android.widget.Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        
                        onSave(Habit(
                            habitId = initialHabit?.habitId ?: UUID.randomUUID().toString(),
                            userId = initialHabit?.userId ?: "",
                            title = title.trim(),
                            iconKey = selectedIconKey,
                            colorTag = selectedColor,
                            frequency = frequency,
                            customDays = selectedCustomDays,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            currentStreak = initialHabit?.currentStreak ?: 0,
                            longestStreak = initialHabit?.longestStreak ?: 0,
                            growthPoints = initialHabit?.growthPoints ?: 0,
                            archived = initialHabit?.archived ?: false,
                            sortOrder = initialHabit?.sortOrder ?: 0,
                            createdAt = initialHabit?.createdAt ?: System.currentTimeMillis()
                        ))
                     }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Text("Habit title", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Take a walk") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            
            item {
                Text("Choose an icon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val chunkedIcons = icons.chunked(4)
                    chunkedIcons.forEach { rowIcons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowIcons.forEach { (key, resId) ->
                                val isSelected = selectedIconKey == key
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            if (isSelected) 3.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedIconKey = key },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        colorFilter = ColorFilter.tint(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Color tag", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    colors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(getColorFromString(color))
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                val checkColor = if (color == "butter") Color.Black else Color.White
                                Text(
                                    text = "✓",
                                    color = checkColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Frequency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Pair("Daily", "daily"),
                        Pair("Weekdays", "weekdays"),
                        Pair("Custom days", "custom")
                    ).forEach { (label, value) ->
                        FilterChip(
                            selected = frequency == value,
                            onClick = { 
                                frequency = value
                                if (value != "custom") {
                                    selectedCustomDays = emptyList()
                                }
                            },
                            label = { Text(label) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                
                if (frequency == "custom") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select days of the week", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = listOf(
                            Pair("M", 1),
                            Pair("T", 2),
                            Pair("W", 3),
                            Pair("T", 4),
                            Pair("F", 5),
                            Pair("S", 6),
                            Pair("S", 7)
                        )
                        daysOfWeek.forEach { (label, dayVal) ->
                            val isDaySelected = selectedCustomDays.contains(dayVal)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDaySelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (isDaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .clickable {
                                        selectedCustomDays = if (isDaySelected) {
                                            selectedCustomDays - dayVal
                                        } else {
                                            selectedCustomDays + dayVal
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDaySelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reminder time", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(reminderTime, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reminder", style = MaterialTheme.typography.labelLarge)
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showTimePicker) {
        TimeSelectionDialog(
            initialTime = reminderTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { selected ->
                reminderTime = selected
                showTimePicker = false
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

/** Delegates to [PlantTag] so the picker and the habit cards can never disagree. */
fun getColorFromString(name: String): Color = PlantTag.fromKey(name).accent
