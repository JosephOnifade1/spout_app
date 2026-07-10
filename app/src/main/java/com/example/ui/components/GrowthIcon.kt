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
import com.example.R
import com.example.data.GrowthStatus

@Composable
fun GrowthIcon(status: GrowthStatus, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "growthScale"
    )

    val iconRes = when (status) {
        GrowthStatus.SEED -> R.drawable.ic_status_seed
        GrowthStatus.SPROUT -> R.drawable.ic_status_sprout
        GrowthStatus.BUD -> R.drawable.ic_status_bud
        GrowthStatus.BLOOM -> R.drawable.ic_status_bloom
        GrowthStatus.RESTING -> R.drawable.ic_status_resting
    }

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = status.value,
        modifier = modifier
            .size(32.dp)
            .scale(scale)
    )
}
