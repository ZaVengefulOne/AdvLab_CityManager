package org.vengeful.citymanager.screens.niis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.niis.*
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun SeveriteCleaningScreen(
    navController: NavController,
    sampleNumber: String
) {
    val viewModel: SeveritCleaningViewModel = koinViewModel()
    val currentTheme = LocalTheme

    val cleaningMode by viewModel.cleaningMode.collectAsState()
    val activeIndices by viewModel.activeIndices.collectAsState()
    val currentValues by viewModel.currentValues.collectAsState()
    val guessedIndices by viewModel.guessedIndices.collectAsState()
    val showSuccessDialog by viewModel.showSuccessDialog.collectAsState()
    val targetSequence by viewModel.targetSequence.collectAsState()

    // Обновляем подсказки при изменении targetSequence
    val dialLockHints = remember(targetSequence) {
        viewModel.getDialLockHints()
    }

    LaunchedEffect(sampleNumber) {
        viewModel.initializeMode(sampleNumber)
    }

    VengBackground(theme = currentTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Кнопка назад и прогресс-бар с подписью
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengButton(
                    onClick = {
                        viewModel.reset()
                        navController.popBackStack()
                    },
                    text = "Назад",
                    theme = currentTheme,
                    modifier = Modifier.width(120.dp)
                )

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VengText(
                        text = "Процесс очистки",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SegmentedProgressBar(
                        progress = guessedIndices.size,
                        totalSegments = activeIndices.size,
                        modifier = Modifier.fillMaxWidth(),
                        theme = currentTheme
                    )
                }
            }

            // Все возможные элементы
            val allElements = listOf(
                ElementData(0, "Регулятор давления") { value, onChange ->
                    SteampunkSlider(
                        value = value,
                        onValueChange = onChange,
                        theme = currentTheme,
                        label = "Регулятор давления"
                    )
                },
                ElementData(1, "Комбинационный замок") { value, onChange ->
                    SteampunkDialLock(
                        value = value,
                        onValueChange = onChange,
                        hints = dialLockHints,
                        targetValue = targetSequence.getOrNull(1),
                        theme = currentTheme,
                        label = "Комбинационный замок"
                    )
                },
                ElementData(2, "Индикатор уровня") { value, onChange ->
                    SteampunkSegmentPuzzle(
                        value = value,
                        onValueChange = onChange,
                        theme = currentTheme,
                        label = "Индикатор уровня"
                    )
                },
                ElementData(3, "Круговой регулятор") { value, onChange ->
                    SteampunkDial(
                        value = value,
                        onValueChange = onChange,
                        theme = currentTheme,
                        label = "Круговой регулятор"
                    )
                },
                ElementData(4, "Способы очистки") { value, onChange ->
                    SteampunkSwitchPuzzle(
                        value = value,
                        onValueChange = onChange,
                        targetValue = targetSequence.getOrNull(4),
                        theme = currentTheme,
                        label = "Способы очистки"
                    )
                },
                ElementData(5, "Регуляторное колесо") { value, onChange ->
                    SteampunkWheel(
                        value = value,
                        onValueChange = onChange,
                        theme = currentTheme,
                        label = "Регуляторное колесо"
                    )
                },
                ElementData(6, "Управляющий рычаг") { value, onChange ->
                    SteampunkLever(
                        value = value,
                        onValueChange = onChange,
                        theme = currentTheme,
                        label = "Управляющий рычаг"
                    )
                }
            )

            // Фильтруем элементы по активным индексам
            val activeElements = allElements.filter { it.index in activeIndices }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(activeElements) { element ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            element.content(
                                currentValues[element.index],
                                { viewModel.updateValue(element.index, it) }
                            )
                        }
                        SuccessIndicator(isActive = guessedIndices.contains(element.index))
                    }
                }
            }
        }
    }

    // Диалог успеха
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissSuccessDialog()
                viewModel.reset()
                navController.popBackStack()
            },
            title = {
                VengText(
                    text = "Очистка проведена успешно!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                VengText(
                    text = "Все параметры установлены корректно.\nСеверит очищен.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                VengButton(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        viewModel.reset()
                        navController.popBackStack()
                    },
                    text = "ОК",
                    theme = currentTheme
                )
            },
            containerColor = SeveritepunkThemes.getColorScheme(currentTheme).background
        )
    }
}

private data class ElementData(
    val index: Int,
    val label: String,
    val content: @Composable (Int, (Int) -> Unit) -> Unit
)
