package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.LocalPlantMotionEnabled
import com.example.ui.components.PlantFraming
import com.example.ui.components.PlantPortrait
import com.example.ui.components.PlantStage
import com.example.ui.theme.BloomPink
import com.example.ui.theme.Butter
import com.example.ui.theme.DustyClay
import com.example.ui.theme.InkTeal
import com.example.ui.theme.Moss
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PerennialTypography
import com.example.ui.theme.SoilDeep
import kotlin.math.sin

/**
 * The 7 / 30 / 100 moment.
 *
 * Previously a rounded card with a flat glyph, the words "growth points" under a day count, and
 * a lot of dead space — it read like a receipt. This is a full-bleed celebration: butter light,
 * petals drifting out of the soil, the plant breathing, the number counting up. Skips on tap and
 * finishes inside ~1.8s, so it never becomes something to dismiss.
 */
@Composable
fun MilestoneModal(
    streak: Int,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
    habitTitle: String? = null,
    encouragement: String? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        MilestoneCelebration(
            streak = streak,
            onShare = onShare,
            onDismiss = onDismiss,
            habitTitle = habitTitle,
            encouragement = encouragement
        )
    }
}

/**
 * The celebration itself, without the window around it.
 *
 * Split out because a Dialog renders in its own window, which Robolectric screenshots do not
 * reliably capture — rendering this directly is what makes the milestone reviewable in CI.
 */
@Composable
fun MilestoneCelebration(
    streak: Int,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
    habitTitle: String? = null,
    encouragement: String? = null
) {
    val motionEnabled = LocalPlantMotionEnabled.current

    Surface(
        color = Butter,
        modifier = Modifier
            .fillMaxSize()
            // Tap anywhere to skip — the celebration is not something to be trapped in.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PetalDrift(modifier = Modifier.fillMaxSize(), enabled = motionEnabled)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 34.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MILESTONE",
                    style = MaterialTheme.typography.labelSmall,
                    // DustyClay on butter is ~2.3:1 — unreadable. Warm brown reads at ~6:1.
                    color = SoilDeep
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "You've nurtured this for",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedInk(),
                    textAlign = TextAlign.Center
                )

                MilestoneCount(streak = streak, enabled = motionEnabled)

                Text(
                    text = if (streak == 1) "day" else "days",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedInk(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                BreathingPlant(enabled = motionEnabled)

                Spacer(Modifier.height(16.dp))

                Text(
                    text = habitTitle ?: "your garden",
                    style = MaterialTheme.typography.headlineSmall,
                    color = InkTeal,
                    textAlign = TextAlign.Center
                )

                if (!encouragement.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = encouragement,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Moss,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Share", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, InkTeal.copy(alpha = 0.25f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.55f),
                            contentColor = InkTeal
                        )
                    ) {
                        Text("Keep growing", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/** Muted ink against butter; butter is light, so ink at 70% reads calmer than black. */
@Composable
private fun MutedInk(): Color = InkTeal.copy(alpha = 0.70f)

@Composable
private fun MilestoneCount(streak: Int, enabled: Boolean) {
    var target by remember { mutableIntStateOf(if (enabled) 0 else streak) }
    LaunchedEffect(streak, enabled) { if (enabled) target = streak }

    val value by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900),
        label = "milestoneCount"
    )

    Text(
        text = value.toString(),
        style = PerennialTypography.HeadlineLarge.copy(fontSize = 92.sp, lineHeight = 100.sp),
        color = InkTeal,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
private fun BreathingPlant(enabled: Boolean) {
    val scale: Float = if (enabled) {
        val transition = rememberInfiniteTransition(label = "breathe")
        val breathe by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.045f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breatheScale"
        )
        breathe
    } else {
        1f
    }

    // Framed on cream: the illustration is opaque paper, so an unframed square on butter would
    // look pasted in rather than part of the scene.
    Surface(
        color = PaperCream,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.size(176.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            PlantPortrait(
                stage = PlantStage.BLOOM,
                size = 166.dp,
                framing = PlantFraming.CARD,
                sway = enabled,
                swayPeriodMillis = 3400,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
    }
}

/**
 * Petals and husk fragments drifting up from the soil line.
 *
 * No infinite transition when motion is off: an animation that never settles stops Compose from
 * reaching idle, and screenshot tests would hang.
 */
@Composable
private fun PetalDrift(modifier: Modifier = Modifier, enabled: Boolean) {
    if (!enabled) return

    val transition = rememberInfiniteTransition(label = "petalDrift")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "petalPhase"
    )

    Canvas(modifier = modifier) {
        val palette = listOf(BloomPink, DustyClay, Moss, SoilDeep)
        repeat(18) { index ->
            // Deterministic scatter — no Random, so any given frame renders identically.
            val across = ((index * 61) % 100) / 100f
            val lagging = ((index * 37) % 100) / 100f
            val progress = (phase + lagging) % 1f

            val x = size.width * (0.06f + 0.88f * across) + sin(progress * 6f + index) * 16f
            val y = size.height * (1.04f - progress * 0.88f)
            val radius = 3.5f + (index % 3)
            val alpha = (1f - progress).coerceIn(0f, 1f) * 0.55f

            drawCircle(
                color = palette[index % palette.size].copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}
