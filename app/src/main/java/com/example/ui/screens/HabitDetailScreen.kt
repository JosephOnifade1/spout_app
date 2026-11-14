package com.example.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CheckIn
import com.example.data.Habit
import com.example.ui.components.LocalPlantMotionEnabled
import com.example.ui.components.PlantFraming
import com.example.ui.components.PlantPortrait
import com.example.ui.components.PlantStage
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PerennialTypography
import java.time.LocalDate

/**
 * The habit's own page: one plant, large, plus the week that produced it.
 *
 * The previous version showed the habit's 80dp glyph — the same icon as the list row — above a
 * record of small monochrome status dots, and never showed the plant at all.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit,
    recentCheckIns: List<CheckIn>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onArchive: (Habit) -> Unit,
    onUnarchive: (Habit) -> Unit,
    // Injectable so the growth bed's weekday letters don't shift under screenshot tests.
    today: LocalDate = LocalDate.now()
) {
    val stage = PlantStage.fromStreak(habit.currentStreak)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("←", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = { }) {
                    Text("⋮", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Edit", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedButton(
                        onClick = { if (habit.archived) onUnarchive(habit) else onArchive(habit) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = if (habit.archived) "Unarchive" else "Archive",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(habit.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = habitSubtitle(habit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(18.dp))

            // Hero: the whole composition, on the same paper it was painted on, so the portrait
            // fills the card instead of sitting in a box.
            Surface(
                color = PaperCream,
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                modifier = Modifier.fillMaxWidth().height(272.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PlantPortrait(
                        stage = stage,
                        size = 250.dp,
                        framing = PlantFraming.FULL,
                        sway = true,
                        swayPeriodMillis = 5200
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRENT STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = streakRollUp(habit.currentStreak),
                            style = PerennialTypography.TechnicalMono.copy(fontSize = 46.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (habit.currentStreak == 1) "day" else "days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "STAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted
                    )
                    Spacer(Modifier.height(6.dp))
                    StageChip(stage)
                }
            }

            Spacer(Modifier.height(26.dp))

            GrowthBed(habit = habit, recentCheckIns = recentCheckIns, today = today)

            Spacer(Modifier.height(20.dp))

            Text(
                text = nextStageLine(habit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

/** Counts up from zero on entry. Skipped when motion is off so screenshots are deterministic. */
@Composable
private fun streakRollUp(streak: Int): String {
    val motionEnabled = LocalPlantMotionEnabled.current
    var target by remember { mutableIntStateOf(if (motionEnabled) 0 else streak) }

    LaunchedEffect(streak, motionEnabled) {
        if (motionEnabled) target = streak
    }

    val value by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900),
        label = "streakRollUp"
    )
    // No zero padding: "05 days" reads like a serial number, "5 days" reads like a sentence.
    return value.toString()
}

@Composable
private fun StageChip(stage: PlantStage) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantPortrait(stage = stage, size = 18.dp, framing = PlantFraming.CARD)
            Spacer(Modifier.width(7.dp))
            Text(
                text = stage.displayName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Seven days, each drawn as the plant you actually had at the end of that day — not a dot.
 * Days the habit wasn't scheduled for are shown as an empty slot rather than a failure.
 */
@Composable
private fun GrowthBed(habit: Habit, recentCheckIns: List<CheckIn>, today: LocalDate) {
    val days = remember(habit, recentCheckIns, today) {
        (0..6).reversed().map { offset ->
            val date = today.minusDays(offset.toLong())
            val checkIn = recentCheckIns.find { it.date == date.toString() }
            BedDay(
                date = date,
                scheduled = isScheduled(habit, date),
                stage = checkIn?.let { stageFromStatus(it.status) }
            )
        }
    }

    Column {
        Text(
            text = "THIS WEEK",
            style = MaterialTheme.typography.labelSmall,
            color = InkMuted
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (day.scheduled) PaperCream else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            !day.scheduled -> Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                            )
                            day.stage != null -> PlantPortrait(
                                stage = day.stage,
                                size = 46.dp,
                                framing = PlantFraming.CARD,
                                animated = false
                            )
                            else -> Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = day.date.dayOfWeek.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = InkMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class BedDay(
    val date: LocalDate,
    val scheduled: Boolean,
    val stage: PlantStage?
)

private fun stageFromStatus(status: String): PlantStage = when (status.lowercase()) {
    "bloom" -> PlantStage.BLOOM
    "bud" -> PlantStage.BUD
    "perennial", "sprout" -> PlantStage.SPROUT
    "resting" -> PlantStage.RESTING
    else -> PlantStage.SEED
}

private fun habitSubtitle(habit: Habit): String {
    val target = habit.targetText?.let { "$it · " } ?: ""
    return "$target${habit.frequency} · remind at ${habit.reminderTime}"
}

private fun nextStageLine(habit: Habit): String {
    val remaining = PlantStage.daysToNext(habit.currentStreak)
    return if (remaining == null) {
        "In full bloom. Keep going to make it a perennial."
    } else {
        "$remaining more ${if (remaining == 1) "day" else "days"} until this plant reaches the next stage."
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
