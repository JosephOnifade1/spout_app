package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Habit
import com.example.ui.theme.SproutTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedHabitsScreen(
    habits: List<Habit>,
    onBack: () -> Unit,
    onRestore: (Habit) -> Unit,
    onDeletePermanently: (Habit) -> Unit
) {
    val archivedHabits = habits.filter { it.archived }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    if (habitToDelete != null) {
        DeleteConfirmationDialog(
            habit = habitToDelete!!,
            onDismiss = { habitToDelete = null },
            onConfirm = {
                onDeletePermanently(habitToDelete!!)
                habitToDelete = null
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Archived Habits", style = SproutTypography.HeadlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (archivedHabits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "No archived habits.",
                    style = SproutTypography.BodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(archivedHabits) { habit ->
                    ArchivedHabitRow(
                        habit = habit,
                        onRestore = { onRestore(habit) },
                        onDelete = { habitToDelete = habit }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                    )
                }
            }
        }
    }
}

@Composable
fun ArchivedHabitRow(
    habit: Habit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.title, style = SproutTypography.BodyLarge)
            Text(
                "Archived", 
                style = SproutTypography.LabelSmallMono,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
        
        Row {
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Outlined.Restore, 
                    contentDescription = "Restore",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete, 
                    contentDescription = "Delete permanently",
                    tint = Color(0xFFB08968) // Muted dusty-clay tone
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Delete '${habit.title}' permanently?", 
                style = SproutTypography.TitleMedium
            ) 
        },
        text = {
            Text(
                "This will remove all check-in history and milestones for this habit. This can't be undone.",
                style = SproutTypography.BodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB08968))
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}
