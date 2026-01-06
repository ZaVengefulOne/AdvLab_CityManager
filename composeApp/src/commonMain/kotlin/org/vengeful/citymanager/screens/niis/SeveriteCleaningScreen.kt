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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.niis.*
import org.vengeful.citymanager.ROUTE_MAIN
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme
import org.vengeful.citymanager.audio.rememberSoundPlayer

@Composable
fun SeveriteCleaningScreen(
    navController: NavController,
    sampleNumber: String
) {
    val viewModel: SeveritCleaningViewModel = koinViewModel()
    val currentTheme = LocalTheme
    val soundPlayer = rememberSoundPlayer()
    val coroutineScope = rememberCoroutineScope()

    val cleaningMode by viewModel.cleaningMode.collectAsState()
    val activeIndices by viewModel.activeIndices.collectAsState()
    val currentValues by viewModel.currentValues.collectAsState()
    val guessedIndices by viewModel.guessedIndices.collectAsState()
    val showSuccessDialog by viewModel.showSuccessDialog.collectAsState()
    val targetSequence by viewModel.targetSequence.collectAsState()
    val overheatProgress by viewModel.overheatProgress.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val showErrorDialog by viewModel.showErrorDialog.collectAsState()

    // Настройки эффекта покраснения (можно экспериментировать)
    val overheatThreshold = 0.5f // Порог начала эффекта (50%)
    val overheatMaxIntensity = 0.4f // Максимальная интенсивность эффекта

    // Обновляем подсказки при изменении targetSequence
    val dialLockHints = remember(targetSequence) {
        viewModel.getDialLockHints()
    }

    // Звук работы системы при инициализации
    LaunchedEffect(sampleNumber) {
        viewModel.initializeMode(sampleNumber)
        // Запускаем звук в фоновом потоке, чтобы не блокировать UI
        launch(Dispatchers.IO) {
            soundPlayer.playSystemWorkingSound()
        }
    }

    // Останавливаем звук работы системы при успехе
    LaunchedEffect(showSuccessDialog) {
        if (showSuccessDialog) {
            launch(Dispatchers.IO) {
                soundPlayer.stopSystemWorkingSound()
            }
        }
    }

    // Звук выключения системы при истечении таймера и остановка звука работы
    LaunchedEffect(showErrorDialog) {
        if (showErrorDialog) {
            // Останавливаем звук работы системы
            launch(Dispatchers.IO) {
                soundPlayer.stopSystemWorkingSound()
                // Запускаем звук выключения
                soundPlayer.playSystemShutdownSound()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            soundPlayer.stopSystemWorkingSound()
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
                        VengText(
                            text = "Температура системы:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OverheatProgressBar(
                            progress = overheatProgress,
                            modifier = Modifier.fillMaxWidth(),
                            theme = currentTheme
                        )
                        VengText(
                            text = "Осталось времени: ${formatTime(remainingTime)}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
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
                                    currentValues[element.index]
                                ) { newValue ->
                                    // Запускаем звук в фоновом потоке, чтобы не блокировать UI
                                    coroutineScope.launch(Dispatchers.IO) {
                                        soundPlayer.playClickSound() // Звук клика при изменении значения
                                    }
                                    viewModel.updateValue(element.index, newValue)
                                }
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

        // Диалог ошибки
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.dismissErrorDialog()
                    viewModel.reset()
                    soundPlayer.stopSystemWorkingSound()
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_MAIN) {
                            inclusive = true
                        }
                    }
                },
                title = {
                    VengText(
                        text = "Ошибка очистки",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    VengText(
                        text = "Перегрев! \n Очистка северита не была выполнена.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    VengButton(
                        onClick = {
                            viewModel.dismissErrorDialog()
                            viewModel.reset()
                            soundPlayer.stopSystemWorkingSound()
                            navController.navigate(ROUTE_MAIN) {
                                popUpTo(ROUTE_MAIN) {
                                    inclusive = true
                                }
                            }
                        },
                        text = "ОК",
                        theme = currentTheme
                    )
                },
                containerColor = SeveritepunkThemes.getColorScheme(currentTheme).background
            )
        }
    }
}

@Suppress("DefaultLocale")
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private data class ElementData(
    val index: Int,
    val label: String,
    val content: @Composable (Int, (Int) -> Unit) -> Unit
)
