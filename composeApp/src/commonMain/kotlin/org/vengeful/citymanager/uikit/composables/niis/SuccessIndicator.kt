package org.vengeful.citymanager.uikit.composables.niis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SuccessIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.8f,
        animationSpec = tween(300),
        label = "indicator_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "indicator_alpha"
    )

    Box(
        modifier = modifier
            .size(20.dp)
            .scale(scale)
            .background(
                if (isActive) Color(0xFF4CAF50) else Color.Gray.copy(alpha = alpha),
                CircleShape
            )
            .border(
                width = 2.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isActive) Color(0xFF81C784) else Color.Gray,
                        if (isActive) Color(0xFF2E7D32) else Color.DarkGray
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFA5D6A7), CircleShape)
            )
        }
    }
}

