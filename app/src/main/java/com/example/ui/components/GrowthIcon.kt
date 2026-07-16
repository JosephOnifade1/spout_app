package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.data.GrowthStatus
import com.example.ui.theme.AppIcons

@Composable
fun GrowthIcon(status: GrowthStatus, modifier: Modifier = Modifier) {
    if (status == GrowthStatus.BLANK) {
        androidx.compose.foundation.layout.Spacer(modifier = modifier.size(32.dp))
        return
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "growthScale"
    )

    if (status == GrowthStatus.UNSCHEDULED) {
        androidx.compose.foundation.Canvas(modifier = modifier.size(32.dp)) {
            drawCircle(
                color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.5f),
                radius = 4.dp.toPx()
            )
        }
        return
    }

    val iconRes = when (status) {
        GrowthStatus.SEED -> AppIcons.GrowthSeed
        GrowthStatus.SPROUT -> AppIcons.GrowthSprout
        GrowthStatus.BUD -> AppIcons.GrowthBud
        GrowthStatus.BLOOM -> AppIcons.GrowthBloom
        GrowthStatus.RESTING -> AppIcons.GrowthResting
        else -> AppIcons.GrowthSeed
    }

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = status.value,
        modifier = modifier
            .size(32.dp)
            .scale(scale)
    )
}
