package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.Milestone
import com.example.ui.components.BottomNavBar
import com.example.ui.theme.AppIcons
import com.example.ui.theme.PerennialTypography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestonesScreen(
    milestones: List<Milestone>,
    habits: List<Habit>,
    onNotificationsClick: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val inProgressMilestones = remember(habits) {
        habits.filter { !it.archived }.mapNotNull { habit ->
            val nextTier = when {
                habit.growthPoints < 10 -> 10
                habit.growthPoints < 25 -> 25
                habit.growthPoints < 50 -> 50
                habit.growthPoints < 100 -> 100
                else -> null
            }
            if (nextTier != null) {
                habit to nextTier
            } else null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = AppIcons.PerennialOutline),
                        contentDescription = "Perennial icon",
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
                Text(
                    "Milestones",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = PerennialTypography.HeadlineLarge
                )
                Text(
                    "A journal of your growth moments.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = PerennialTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (milestones.isEmpty() && inProgressMilestones.isEmpty()) {
                EmptyMilestonesState()
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Timeline Line
                    Canvas(modifier = Modifier.fillMaxHeight().width(64.dp).padding(start = 48.dp)) {
                        drawLine(
                            color = Color(0xFFC4D1C5), // Light mossy line
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
                    ) {
                        itemsIndexed(milestones) { index, milestone ->
                            MilestoneCard(milestone = milestone, index = index)
                        }

                        itemsIndexed(inProgressMilestones) { index, (habit, nextTier) ->
                            InProgressMilestoneCard(
                                habit = habit,
                                nextTier = nextTier,
                                index = milestones.size + index
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneCard(milestone: Milestone, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 600),
        label = "offsetY"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .graphicsLayer(
                alpha = alpha,
                translationY = offsetY.value
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marker
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFE8F0E9), CircleShape), // Moss tinted circle
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = AppIcons.GrowthBloom),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        milestone.habitTitle,
                        style = PerennialTypography.BodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        milestone.dateReached,
                        style = PerennialTypography.LabelSmallMono,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Text(
                    "${milestone.tier} growth points",
                    style = PerennialTypography.BodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    milestone.encouragementLine,
                    style = PerennialTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun InProgressMilestoneCard(habit: Habit, nextTier: Int, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 600),
        label = "offsetY"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .graphicsLayer(
                alpha = alpha,
                translationY = offsetY.value
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marker
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFF1F5F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = AppIcons.GrowthBud),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Card with dashed border
        val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    habit.title,
                    style = PerennialTypography.BodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "In progress",
                    style = PerennialTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progress = habit.growthPoints.toFloat() / nextTier.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "${habit.growthPoints}/$nextTier",
                        style = PerennialTypography.TechnicalMono.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${habit.growthPoints} of $nextTier points",
                    style = PerennialTypography.BodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyMilestonesState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = AppIcons.GrowthSeed),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Start a habit to grow your first milestone",
            textAlign = TextAlign.Center,
            style = PerennialTypography.BodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
