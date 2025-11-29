package org.vengeful.citymanager.uikit.composables.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkCardColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun BankAccountCard(
    account: BankAccount,
    person: Person?,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val cardColors = remember(theme) {
        SeveritepunkThemes.getCardColors(theme)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(cardColors.background)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(cardColors.borderLight, cardColors.borderDark)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .clickable(enabled = onCardClick != null) { onCardClick?.invoke() }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    VengText(
                        text = "Счёт #${account.id}",
                        color = cardColors.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    VengText(
                        text = person?.let { "${it.firstName} ${it.lastName}" } ?: (account.enterpriseName ?: "Предприятие"),
                        color = cardColors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Индикатор типа
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (person != null) cardColors.accent else Color(0xFF9C27B0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = if (person != null) "👤" else "🏢",
                        fontSize = 10.sp
                    )
                }
            }

            // Разделитель
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(cardColors.accent.copy(alpha = 0.3f))
            )


            // Финансовая информация (компактно)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Вклад
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    VengText(
                        text = "Счёт",
                        color = cardColors.text.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                    VengText(
                        text = String.format("%.0f", account.depositAmount),
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                    )
                }

                // Кредит
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    VengText(
                        text = "Кредит",
                        color = cardColors.text.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                    VengText(
                        text = String.format("%.0f", account.creditAmount),
                        color = Color(0xFFFF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                    )
                }

                // Баланс
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    VengText(
                        text = "Баланс",
                        color = cardColors.accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val balance = account.depositAmount - account.creditAmount
                    VengText(
                        text = String.format("%.0f", balance),
                        color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFFF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                    )
                }
            }
        }
    }
}
