package org.vengeful.citymanager.uikit.composables.administration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.severite.Severite
import org.vengeful.citymanager.models.severite.SeveritePurity
import org.vengeful.citymanager.models.severite.getSeveritePurity
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengCheckBox
import org.vengeful.citymanager.uikit.composables.veng.VengText
import kotlin.math.roundToInt

@Composable
fun SeveriteSalesWidget(
    severites: List<Severite>,
    severiteRate: Double,
    onSell: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2C3E50),
    borderColor: Color = Color(0xFF4A90E2),
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    var showSalesDialog by remember { mutableStateOf(false) }
    var selectedSeveriteIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Группируем северит по типам чистоты
    val groupedSeverites = severites.groupBy { it.purity }

    // Коэффициенты чистоты
    val purityCoefficients = mapOf(
        SeveritePurity.CONTAMINATED to 0.75,
        SeveritePurity.NORMAL to 1.0,
        SeveritePurity.CRYSTAL_CLEAR to 1.5
    )


    // Вычисляем итоговую сумму
    val totalAmount = selectedSeveriteIds.sumOf { id ->
        val severite = severites.find { it.id == id }
        if (severite != null) {
            val coefficient = purityCoefficients[severite.purity] ?: 1.0
            coefficient * severiteRate
        } else {
            0.0
        }
    }

    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VengText(
            text = "Продажа северита",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        VengText(
            text = "В наличии: ${severites.size}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        VengButton(
            onClick = {
                selectedSeveriteIds = emptySet()
                showSalesDialog = true
            },
            text = "Продать северит",
            modifier = Modifier.fillMaxWidth(),
            padding = 8.dp,
            theme = theme,
            enabled = severites.isNotEmpty()
        )
    }

    if (showSalesDialog) {
        AlertDialog(
            onDismissRequest = {
                showSalesDialog = false
                selectedSeveriteIds = emptySet()
            },
            title = {
                VengText(
                    text = "Продажа северита",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Отображаем северит по группам
                    groupedSeverites.forEach { (purity, severiteList) ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val purityName = when (purity) {
                                SeveritePurity.CONTAMINATED -> "Загрязнённый северит"
                                SeveritePurity.NORMAL -> "Обычный северит"
                                SeveritePurity.CRYSTAL_CLEAR -> "Кристально чистый северит"
                            }
                            val coefficient = purityCoefficients[purity] ?: 1.0
                            val pricePerUnit = coefficient * severiteRate

                            VengText(
                                text = "$purityName (${severiteList.size} шт.)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            VengText(
                                text = "Цена за единицу: ${pricePerUnit.roundToInt()}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Чекбоксы для каждого северита
                            severiteList.forEach { severite ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        VengCheckBox(
                                            checked = selectedSeveriteIds.contains(severite.id),
                                            onCheckedChange = { checked ->
                                                selectedSeveriteIds = if (checked) {
                                                    selectedSeveriteIds + severite.id
                                                } else {
                                                    selectedSeveriteIds - severite.id
                                                }
                                            },
                                            theme = theme,
                                        )
                                        VengText(
                                            text = "Чистота: ${getSeveritePurity(severite.purity)}, ID: ${severite.id}",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedSeveriteIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VengText(
                            text = "Итого: ${totalAmount.roundToInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                VengButton(
                    onClick = {
                        if (selectedSeveriteIds.isNotEmpty()) {
                            onSell(selectedSeveriteIds.toList())
                            showSalesDialog = false
                            selectedSeveriteIds = emptySet()
                        }
                    },
                    text = "Продать",
                    theme = theme,
                    enabled = selectedSeveriteIds.isNotEmpty()
                )
            },
            dismissButton = {
                VengButton(
                    onClick = {
                        showSalesDialog = false
                        selectedSeveriteIds = emptySet()
                    },
                    text = "Отмена",
                    theme = theme
                )
            },
            containerColor = SeveritepunkThemes.getColorScheme(theme).background
        )
    }
}

