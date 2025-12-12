package org.vengeful.citymanager.screens.medic

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.medic.MedicineOrderCard
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun MedicOrdersScreen(navController: NavController) {
    val medicViewModel: MedicViewModel = koinViewModel()
    val orders by medicViewModel.medicineOrders.collectAsState()
    val isLoading by medicViewModel.isLoading.collectAsState()

    var currentTheme by remember { mutableStateOf(LocalTheme) }

    LaunchedEffect(Unit) {
        medicViewModel.loadMedicineOrders()
    }

    VengBackground(
        modifier = Modifier.fillMaxSize(),
        theme = currentTheme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок и кнопка назад
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = "Заказы лекарств",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                VengButton(
                    onClick = { navController.popBackStack() },
                    text = "Назад",
                    theme = currentTheme,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Список заказов
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4A90E2),
                        strokeWidth = 2.dp
                    )
                }
            } else if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = "Нет заказов",
                        color = SeveritepunkThemes.getColorScheme(currentTheme).text.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 350.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(orders.reversed()) { order ->
                        MedicineOrderCard(
                            order = order,
                            modifier = Modifier.fillMaxWidth(),
                            theme = currentTheme
                        )
                    }
                }
            }
        }
    }
}
