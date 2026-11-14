package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * The five stages a habit's plant can be in.
 *
 * `RESTING` is deliberately not reachable from [fromStreak]: a broken streak doesn't kill the
 * plant, it stops growing, and the caller decides when to show the dormant art. Keeping the
 * thresholds here (rather than scattered through screens) means Today, Habit Detail and the
 * growth bed can never disagree about what stage a 5-day streak is.
 */
enum class PlantStage(
    val key: String,
    val displayName: String
) {
    SEED("seed", "Seed"),
    SPROUT("sprout", "Sprout"),
    BUD("bud", "Bud"),
    BLOOM("bloom", "Bloom"),
    RESTING("resting", "Resting");

    companion object {
        /** 0–1 days → seed · 2–3 → sprout · 4–6 → bud · 7+ → bloom */
        fun fromStreak(days: Int): PlantStage = when {
            days >= 7 -> BLOOM
            days >= 4 -> BUD
            days >= 2 -> SPROUT
            else -> SEED
        }

        /** Days still needed to reach the next stage, or null at full bloom. */
        fun daysToNext(days: Int): Int? = when {
            days >= 7 -> null
            days >= 4 -> 7 - days
            days >= 2 -> 4 - days
            else -> 2 - days
        }
    }
}

/**
 * Colour tags. Only MOSS has art generated so far (BUILD_PLAN D2) — the rest are generated in
 * Phase 5. The enum exists now so no call site hard-codes "moss" and nothing has to change when
 * the other art lands.
 */
enum class PlantTag(val key: String) {
    MOSS("moss"),
    CLAY("clay"),
    BUTTER("butter"),
    SOIL("soil"),
    INK_TEAL("ink_teal"),
    CHARCOAL("charcoal")
}

/**
 * Two framings ship per stage: CARD is a tighter bottom-centre crop for 60dp tiles, where the
 * full frame would leave the seed lost in empty paper; FULL keeps the whole composition for
 * heroes and the growth bed.
 */
enum class PlantFraming { CARD, FULL }

/**
 * Draws a plant at a given stage.
 *
 * This is the single component every screen uses — there is no second way to draw a plant.
 * Colour tags are not tinted in at runtime: multiply-blending a full-colour illustration tints
 * the soil and the paper along with the leaves and looks like a stain, so each tag gets its own
 * art set instead.
 *
 * @param sway gentle ±1.6° idle rotation. Pass false in lists of more than ~4 plants, where
 *        many simultaneous infinite animations cost more than they're worth.
 */
@Composable
fun PlantPortrait(
    stage: PlantStage,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    framing: PlantFraming = PlantFraming.CARD,
    tag: PlantTag = PlantTag.MOSS,
    sway: Boolean = false,
    animated: Boolean = true,
    contentDescription: String? = null
) {
    val description = contentDescription ?: "Plant at ${stage.displayName.lowercase()} stage"

    val swayAngle: Float = if (sway) {
        val transition = rememberInfiniteTransition(label = "plantSway")
        val angle by transition.animateFloat(
            initialValue = -1.6f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "plantSwayAngle"
        )
        angle
    } else {
        0f
    }

    val imageModifier = modifier
        .size(size)
        .graphicsLayer {
            rotationZ = swayAngle
            // Rotate around the soil line, not the centre, or the plant looks like it's falling.
            transformOrigin = TransformOrigin(0.5f, 0.92f)
        }
        .semantics { this.contentDescription = description }

    if (animated) {
        Crossfade(targetState = stage, animationSpec = tween(600), label = "plantStage") { current ->
            Image(
                painter = painterResource(id = drawableFor(current, framing)),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.Fit
            )
        }
    } else {
        Image(
            painter = painterResource(id = drawableFor(stage, framing)),
            contentDescription = null,
            modifier = imageModifier,
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Art is currently Moss-only. Adding a tag is a `when` branch plus five files in
 * res/drawable-nodpi — no call-site changes.
 */
private fun drawableFor(stage: PlantStage, framing: PlantFraming): Int = when (stage) {
    PlantStage.SEED -> if (framing == PlantFraming.CARD) R.drawable.plant_seed_card else R.drawable.plant_seed_full
    PlantStage.SPROUT -> if (framing == PlantFraming.CARD) R.drawable.plant_sprout_card else R.drawable.plant_sprout_full
    PlantStage.BUD -> if (framing == PlantFraming.CARD) R.drawable.plant_bud_card else R.drawable.plant_bud_full
    PlantStage.BLOOM -> if (framing == PlantFraming.CARD) R.drawable.plant_bloom_card else R.drawable.plant_bloom_full
    PlantStage.RESTING -> if (framing == PlantFraming.CARD) R.drawable.plant_resting_card else R.drawable.plant_resting_full
}
