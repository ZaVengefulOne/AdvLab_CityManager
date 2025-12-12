package org.vengeful.citymanager.uikit.composables.medic


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.medicine.MedicineOrderNotification
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkCardColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicineOrderCard(
    order: MedicineOrderNotification,
    modifier: Modifier = Modifier,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val cardColors = remember(theme) {
        SeveritepunkThemes.getCardColors(theme)
    }

    val statusColor = when (order.status) {
        "pending" -> Color(0xFFF39C12) // Оранжевый
        "delivering" -> Color(0xFF3498DB) // Синий
        "delivered" -> Color(0xFF27AE60) // Зеленый
        else -> Color(0xFF7B9EB0) // Серый
    }

    val statusText = when (order.status) {
        "pending" -> "Ожидает доставки"
        "delivering" -> "Доставляется"
        "delivered" -> "Доставлено"
        else -> order.status
    }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(order.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColors.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColors.background)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(cardColors.borderLight, cardColors.borderDark)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Заголовок - название лекарства и дата
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = order.medicineName,
                    color = cardColors.accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                VengText(
                    text = dateString,
                    color = cardColors.text.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }

            // Количество
            VengText(
                text = "Количество: ${order.quantity} шт.",
                color = cardColors.text,
                fontSize = 14.sp
            )

            // Сумма
            VengText(
                text = "Сумма: ${String.format("%.2f", order.totalPrice)} ЛБ",
                color = cardColors.text,
                fontSize = 14.sp
            )

            // Заказано от
            VengText(
                text = when {
                    order.orderedByEnterprise != null -> "Заказано от: ${order.orderedByEnterprise}"
                    order.orderedByPersonName != null -> "Заказано: ${order.orderedByPersonName}"
                    else -> "Заказано: Неизвестно"
                },
                color = cardColors.text.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            // Статус
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                VengText(
                    text = statusText,
                    color = statusColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
