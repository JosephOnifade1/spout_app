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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.Butter
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.DustyClay
import com.example.ui.theme.InkTeal
import com.example.ui.theme.Moss
import com.example.ui.theme.Soil

/**
 * Whether idle/ambient motion should run.
 *
 * Two reasons this exists rather than being hard-wired:
 *  1. Accessibility — the OS "remove animations" setting should silence the sway, not just
 *     shorten transitions. Phase 6 wires this to the platform flag.
 *  2. Tests — an infinite animation never lets Compose reach idle, so screenshot tests that
 *     render a garden would hang. Tests provide `false`.
 */
val LocalPlantMotionEnabled = staticCompositionLocalOf { true }

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
 * Colour tags, keyed to the strings already persisted in `Habit.colorTag` — this is what the
 * Add/Edit picker has been writing since the first build, and nothing ever read it back.
 *
 * The tag is rendered as an **accent** (ring, border, streak, stage chip), not by repainting the
 * illustration. Two reasons: thirty near-identical plants in different palettes is a lot of art
 * for very little distinction, and tinting a full-colour painting at runtime stains the soil and
 * the paper along with the leaves. An accent is legible at 38dp and costs nothing.
 * [accent] is the single source of truth for what a tag looks like.
 */
enum class PlantTag(val key: String, val accent: Color) {
    MOSS("moss", Moss),
    CLAY("dustyClay", DustyClay),
    BUTTER("butter", Butter),
    SOIL("soil", Soil),
    INK_TEAL("inkTeal", InkTeal),
    CHARCOAL("charcoalInk", CharcoalInk);

    /** Ink that reads on top of [accent] — butter is light enough to need dark text. */
    val onAccent: Color
        get() = if (this == BUTTER) InkTeal else Color.White

    companion object {
        /** Tolerant lookup: unknown, null or legacy values fall back to Moss rather than throw. */
        fun fromKey(raw: String?): PlantTag {
            val normalised = raw?.lowercase()?.filter { it.isLetterOrDigit() } ?: return MOSS
            return entries.firstOrNull {
                it.key.lowercase().filter { c -> c.isLetterOrDigit() } == normalised
            } ?: MOSS
        }
    }
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
 * @param swayPeriodMillis one half-cycle. Give neighbouring plants different values so they
 *        don't sway in lockstep.
 */
@Composable
fun PlantPortrait(
    stage: PlantStage,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    framing: PlantFraming = PlantFraming.CARD,
    tag: PlantTag = PlantTag.MOSS,
    sway: Boolean = false,
    swayPeriodMillis: Int = 2600,
    animated: Boolean = true,
    contentDescription: String? = null
) {
    val description = contentDescription ?: "Plant at ${stage.displayName.lowercase()} stage"

    val swayAngle: Float = if (sway && LocalPlantMotionEnabled.current) {
        val transition = rememberInfiniteTransition(label = "plantSway")
        val angle by transition.animateFloat(
            initialValue = -1.6f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = swayPeriodMillis, easing = FastOutSlowInEasing),
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
                painter = painterResource(id = drawableFor(current, framing, tag)),
                contentDescription = null,
                modifier = imageModifier,
                contentScale = ContentScale.Fit
            )
        }
    } else {
        Image(
            painter = painterResource(id = drawableFor(stage, framing, tag)),
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
/** Art is Moss-only for now; [tag] is threaded so per-tag sets become a one-line change. */
@Suppress("UNUSED_PARAMETER")
private fun drawableFor(stage: PlantStage, framing: PlantFraming, tag: PlantTag): Int = when (stage) {
    PlantStage.SEED -> if (framing == PlantFraming.CARD) R.drawable.plant_seed_card else R.drawable.plant_seed_full
    PlantStage.SPROUT -> if (framing == PlantFraming.CARD) R.drawable.plant_sprout_card else R.drawable.plant_sprout_full
    PlantStage.BUD -> if (framing == PlantFraming.CARD) R.drawable.plant_bud_card else R.drawable.plant_bud_full
    PlantStage.BLOOM -> if (framing == PlantFraming.CARD) R.drawable.plant_bloom_card else R.drawable.plant_bloom_full
    PlantStage.RESTING -> if (framing == PlantFraming.CARD) R.drawable.plant_resting_card else R.drawable.plant_resting_full
}
