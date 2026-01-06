package org.vengeful.citymanager.screens.stocks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.stocks.StockGraph
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme
import org.vengeful.citymanager.utilities.StockColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(navController: NavController) {
    val viewModel: StockViewModel = koinViewModel()
    val stocks = viewModel.stocks.collectAsState().value
    val currentTheme = LocalTheme

    LaunchedEffect(Unit) {
        viewModel.loadStocks()
    }

    VengBackground(
        modifier = Modifier.fillMaxSize(),
        theme = currentTheme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = "Котировки акций",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                VengButton(
                    text = "Назад",
                    onClick = { navController.popBackStack() },
                    theme = currentTheme
                )
            }

            if (stocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    VengText(
                        text = "Нет доступных акций",
                        color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(stocks.values.toList()) { stockData ->
                        StockGraph(
                            stockName = stockData.config.name,
                            currentPrice = stockData.currentPrice.toInt(),
                            history = stockData.history,
                            modifier = Modifier.fillMaxWidth(),
                            graphColor = StockColors.getColorForIndex(stockData.colorIndex),  // Используем цвет из индекса
                            backgroundColor = SeveritepunkThemes.getColorScheme(currentTheme).background.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
