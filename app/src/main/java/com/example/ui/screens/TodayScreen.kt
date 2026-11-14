package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CheckIn
import com.example.data.Habit
import com.example.ui.components.PlantFraming
import com.example.ui.components.PlantPortrait
import com.example.ui.components.PlantStage
import com.example.ui.theme.AppIcons
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PaperCream
import java.time.LocalDate
import java.time.LocalTime

/**
 * Today is where the product either works or doesn't: it's the screen people open 30 times a
 * day. The rewrite that came out of the UI audit puts a real plant on the page (garden strip +
 * per-habit portrait), moves on check-in, and stops being an empty page for a new user.
 */
@Composable
fun TodayScreen(
    userName: String,
    habits: List<Habit>,
    allCheckIns: List<CheckIn> = emptyList(),
    onAddHabit: () -> Unit,
    onHabitClick: (String) -> Unit,
    onCheckIn: (Habit) -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToTab: (String) -> Unit,
    // Injectable so screenshot tests are deterministic; the greeting otherwise changes
    // with the wall clock and every CI run would produce a different image.
    hourProvider: () -> Int = { LocalTime.now().hour }
) {
    val today = LocalDate.now().toString()
    val sortedHabits = remember(habits) {
        habits.filter { !it.archived }.sortedBy { it.sortOrder }
    }

    var greeting by remember { mutableStateOf(greetingFor(hourProvider())) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // The greeting is recomputed on resume so it doesn't say "Good morning" at 9pm if the app
    // was left open all day.
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                greeting = greetingFor(hourProvider())
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        if (sortedHabits.isEmpty()) {
            EmptyGarden(
                modifier = Modifier.fillMaxSize().padding(padding),
                onAddHabit = onAddHabit
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item(key = "header") {
                    TodayHeader(greeting = greeting, userName = userName, onNotificationsClick = onNotificationsClick)
                }

                item(key = "garden") {
                    GardenStrip(habits = sortedHabits)
                    Spacer(Modifier.height(20.dp))
                }

                items(sortedHabits, key = { it.habitId }) { habit ->
                    val isCheckedIn = allCheckIns.any { it.habitId == habit.habitId && it.date == today }
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 24.dp, vertical = 5.dp)
                    ) {
                        HabitCard(
                            habit = habit,
                            isCheckedIn = isCheckedIn,
                            onClick = { onHabitClick(habit.habitId) },
                            onCheckIn = { onCheckIn(habit) }
                        )
                    }
                }

                item(key = "add") {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onAddHabit,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(54.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add habit", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

private fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning,"
    in 12..16 -> "Good afternoon,"
    in 17..20 -> "Good evening,"
    else -> "Still up?"
}

@Composable
private fun TodayHeader(greeting: String, userName: String, onNotificationsClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = AppIcons.PerennialOutline),
                contentDescription = "Perennial",
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(greeting, style = MaterialTheme.typography.headlineLarge)
        Text(userName, style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Your garden is growing",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
            Spacer(Modifier.width(6.dp))
            Image(
                painter = painterResource(id = AppIcons.GrowthLeaf),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
            )
        }
        Spacer(Modifier.height(18.dp))
    }
}

/**
 * The three furthest-along plants, so the top of Today answers "how am I doing" at a glance
 * instead of being a greeting and a list.
 */
@Composable
private fun GardenStrip(habits: List<Habit>) {
    val featured = remember(habits) { habits.sortedByDescending { it.currentStreak }.take(3) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            // Exactly the paper the plants are painted on (#F6F2E6): any tint here shows up
            // as a lighter square around every illustration.
            .background(PaperCream)
    ) {
        Text(
            text = "YOUR GARDEN",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 14.dp)
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            featured.forEachIndexed { index, habit ->
                PlantPortrait(
                    stage = PlantStage.fromStreak(habit.currentStreak),
                    size = 66.dp,
                    framing = PlantFraming.CARD,
                    sway = true,
                    // Phase-offset so the plants don't move in lockstep.
                    swayPeriodMillis = 2600 + index * 420
                )
            }
        }
    }
}

/**
 * One habit, seen as one plant.
 *
 * The checked state lives on the *card*, not on a tile behind the art: the illustration is
 * opaque and fills its frame, so a tinted tile would never show. Card-level tint plus the
 * filled ring is what actually reads.
 */
@Composable
fun HabitCard(habit: Habit, isCheckedIn: Boolean, onClick: () -> Unit, onCheckIn: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val stage = PlantStage.fromStreak(habit.currentStreak)

    val plantScale by animateFloatAsState(
        targetValue = if (isCheckedIn) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "habitPlantScale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isCheckedIn) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (isCheckedIn) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The art is opaque cream; on a tinted (checked) card an unclipped square reads as
            // a rendering bug. A rounded cream window makes it deliberate.
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(PaperCream)
            ) {
                PlantPortrait(
                    stage = stage,
                    size = 58.dp,
                    framing = PlantFraming.CARD,
                    modifier = Modifier.scale(plantScale)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCheckedIn) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (isCheckedIn) "Goal achieved!" else habitMeta(habit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCheckedIn) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCheckedIn) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            }
                        )
                        .clickable {
                            // The single most repeated action in the app; it gets a haptic.
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCheckIn()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCheckedIn) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked in",
                            modifier = Modifier.size(19.dp),
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (habit.currentStreak > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${habit.currentStreak}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun habitMeta(habit: Habit): String =
    habit.targetText?.let { "Today's target: $it" } ?: "Daily · tap to check in"

/**
 * A brand-new user used to get a greeting followed by empty space and a button. Now the empty
 * state is the first plant: a seed in soil, which is literally what the product is about.
 */
@Composable
private fun EmptyGarden(modifier: Modifier = Modifier, onAddHabit: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Framed deliberately: the illustration is opaque cream, so a bare square against the
        // sage background reads as a missing image. A rounded cream frame makes it a picture.
        Surface(
            color = PaperCream,
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
            modifier = Modifier.size(172.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                PlantPortrait(
                    stage = PlantStage.SEED,
                    size = 158.dp,
                    framing = PlantFraming.CARD,
                    sway = true
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Plant your first habit",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Every habit you keep grows a plant. Start with one small thing you can do today.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(26.dp))
        Button(
            onClick = onAddHabit,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add habit", style = MaterialTheme.typography.titleMedium)
        }
    }
}
