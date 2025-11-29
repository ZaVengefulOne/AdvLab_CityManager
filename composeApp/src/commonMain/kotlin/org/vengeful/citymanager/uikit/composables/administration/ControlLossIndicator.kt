package org.vengeful.citymanager.uikit.composables.administration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun ControlLossIndicator(
    threshold: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2C3E50)
) {
    val indicatorColor = when {
        threshold >= 75 -> Color(0xFF27AE60) // Зелёный
        threshold >= 30 -> Color(0xFFF39C12) // Жёлтый
        else -> Color(0xFFE74C3C) // Красный
    }

    val warningText = when {
        threshold >= 75 -> ""
        threshold >= 30 -> "ВНИМАНИЕ!"
        else -> "КРИТИЧЕСКАЯ ГРАНИЦА! КРАХ НЕИЗБЕЖЕН!"
    }

    Row(
        modifier = modifier
            .background(backgroundColor)
            .border(1.dp, indicatorColor.copy(alpha = 0.3f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Цветовой индикатор
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(indicatorColor, CircleShape)
        )

        // Текст
        Column {
            VengText(
                text = "Граница потери контроля",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            VengText(
                text = threshold.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        VengText(
            text = warningText,
            color = indicatorColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 4
        )
    }
}
