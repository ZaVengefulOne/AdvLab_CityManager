package org.vengeful.citymanager.screens.clicker

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.severite
import org.jetbrains.compose.resources.painterResource
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun ClickerScreen(navController: NavController) {
    val viewModel: ClickerViewModel = koinViewModel()
    val clicks by viewModel.ebanatAmount.collectAsState()
    val hasUpgrade by viewModel.hasSaveProgressUpgrade.collectAsState()
    val hasBankAccount by viewModel.hasBankAccount.collectAsState()
    val clickMultiplier by viewModel.clickMultiplier.collectAsState()
    val theme = LocalTheme

    LaunchedEffect(Unit) {
        viewModel.loadClicks()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveClicks()
        }
    }

    VengBackground(
        modifier = Modifier,
        theme = theme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            VengText(
                text = "Северит-коинов добыто: $clicks",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                color = Color.White
            )

            // Отображение текущего множителя
            VengText(
                text = "Множитель кликов: x$clickMultiplier",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
                color = Color.Yellow
            )

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clickable { viewModel.incrementClicks() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painterResource(resource = Res.drawable.severite), null,
                        modifier = Modifier
                            .size(300.dp)
                    )
                }
                VengText(
                    "Кликни на меня, чтобы добыть!",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка прокачки множителя
            val nextUpgradePrice = viewModel.getNextMultiplierUpgradePrice()
            val canAffordMultiplier = clicks >= nextUpgradePrice
            VengButton(
                onClick = {
                    if (canAffordMultiplier) {
                        viewModel.purchaseClickMultiplierUpgrade()
                    }
                },
                text = if (canAffordMultiplier) {
                    "Улучшить множитель (x${clickMultiplier + 1}) - $nextUpgradePrice коинов"
                } else {
                    "Улучшить множитель (нужно $nextUpgradePrice коинов)"
                },
                theme = theme,
                modifier = Modifier.padding(top = 16.dp),
                enabled = canAffordMultiplier
            )

            // Кнопка покупки улучшения сохранения
            if (!hasUpgrade) {
                val canAfford = clicks >= ClickerConstants.SAVE_PROGRESS_UPGRADE_PRICE
                Spacer(modifier = Modifier.height(16.dp))
                VengButton(
                    onClick = {
                        if (canAfford) {
                            viewModel.purchaseSaveProgressUpgrade()
                        }
                    },
                    text = if (canAfford) {
                        "Купить сохранение прогресса (${ClickerConstants.SAVE_PROGRESS_UPGRADE_PRICE} коинов)"
                    } else {
                        "Купить сохранение прогресса (нужно ${ClickerConstants.SAVE_PROGRESS_UPGRADE_PRICE} коинов)"
                    },
                    theme = theme,
                    enabled = canAfford
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                VengText(
                    text = "✓ Улучшение 'Сохранение прогресса' куплено",
                    fontSize = 16.sp,
                    color = Color.Green
                )
            }

            // Кнопка обмена кликов на деньги
            if (hasBankAccount) {
                val canExchange = clicks >= ClickerConstants.CLICKS_TO_MONEY_EXCHANGE_RATE
                Spacer(modifier = Modifier.height(16.dp))
                VengButton(
                    onClick = {
                        if (canExchange) {
                            viewModel.convertClicksToMoney()
                        }
                    },
                    text = if (canExchange) {
                        "Обменять коины на лабаксы (${ClickerConstants.CLICKS_TO_MONEY_EXCHANGE_RATE} коинов = 1 лабакс)"
                    } else {
                        "Недостаточно коинов для обмена (нужно ${ClickerConstants.CLICKS_TO_MONEY_EXCHANGE_RATE} коинов)"
                    },
                    theme = theme,
                    enabled = canExchange
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Назад"
            VengButton(
                onClick = {
                    viewModel.saveClicks()
                    navController.navigate(ROUTE_MAIN)
                },
                text = "Назад",
                theme = theme,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
