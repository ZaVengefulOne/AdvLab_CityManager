package org.vengeful.citymanager.screens.police

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image as SkiaImage
import org.vengeful.citymanager.data.police.FingerprintsReader
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun FingerprintsBrowser(
    fingerprintsReader: FingerprintsReader,
    policeViewModel: PoliceViewModel,
    onDismiss: () -> Unit,
    onRecordSelected: (Person) -> Unit,
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    val scope = rememberCoroutineScope()
    val fingerprintNumbers = remember { fingerprintsReader.getAllFingerprintNumbers() }
    var selectedFingerprintNumber by remember { mutableStateOf<Int?>(null) }
    var fingerprintImages by remember { mutableStateOf<Map<Int, ImageBitmap?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    // Загружаем изображения отпечатков
    LaunchedEffect(fingerprintNumbers) {
        isLoading = true
        val images = mutableMapOf<Int, ImageBitmap?>()
        
        // Загружаем все изображения параллельно
        coroutineScope {
            val jobs = fingerprintNumbers.map { number ->
                launch {
                    try {
                        val bytes = fingerprintsReader.loadFingerprintImage(number)
                        if (bytes != null) {
                            val skiaImage = SkiaImage.makeFromEncoded(bytes)
                            val bitmap = Bitmap.makeFromImage(skiaImage)
                            images[number] = bitmap.asComposeImageBitmap()
                        } else {
                            images[number] = null
                        }
                    } catch (e: Exception) {
                        println("Error loading fingerprint $number: ${e.message}")
                        images[number] = null
                    }
                }
            }
            
            // Ждем завершения всех загрузок
            jobs.forEach { it.join() }
        }
        
        fingerprintImages = images
        isLoading = false
    }

    val dialogColors = remember(theme) {
        when (theme) {
            ColorTheme.GOLDEN -> DialogColors(
                background = Color(0xFF4A3C2A),
                borderLight = Color(0xFFD4AF37),
                borderDark = Color(0xFF8B7355),
                surface = Color(0xFF5D4A2E)
            )
            ColorTheme.SEVERITE -> DialogColors(
                background = Color(0xFF34495E),
                borderLight = Color(0xFF4A90E2),
                borderDark = Color(0xFF2C3E50),
                surface = Color(0xFF2C3E50)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = dialogColors.background,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(dialogColors.borderLight, dialogColors.borderDark)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .shadow(8.dp, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(dialogColors.surface, dialogColors.background)
                        )
                    )
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                VengText(
                    text = "Просмотр отпечатков пальцев",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        VengText(
                            text = "Загрузка отпечатков...",
                            color = dialogColors.borderLight,
                            fontSize = 16.sp
                        )
                    }
                } else if (fingerprintNumbers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        VengText(
                            text = "Отпечатки не найдены",
                            color = dialogColors.borderLight,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(fingerprintNumbers) { number ->
                            val image = fingerprintImages[number]
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(dialogColors.surface)
                                    .border(
                                        2.dp,
                                        if (selectedFingerprintNumber == number) {
                                            dialogColors.borderLight
                                        } else {
                                            dialogColors.borderDark
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedFingerprintNumber = number
                                        scope.launch {
                                            // Загружаем личное дело по номеру отпечатка
                                            policeViewModel.getPoliceRecordByFingerprintNumber(number)
                                        }
                                    }
                            ) {
                                if (image != null) {
                                    Image(
                                        bitmap = image,
                                        contentDescription = "Отпечаток $number",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    VengText(
                                        text = "№$number\n(не найден)",
                                        color = dialogColors.borderLight,
                                        fontSize = 12.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                VengText(
                                    text = "$number",
                                    color = dialogColors.borderLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(dialogColors.background.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Показываем информацию о выбранном отпечатке
                selectedFingerprintNumber?.let { number ->
                    val currentRecord by policeViewModel.currentRecord.collectAsState()
                    currentRecord?.let { record ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(dialogColors.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            VengText(
                                text = "Личное дело:",
                                color = dialogColors.borderLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            VengText(
                                text = "${record.firstName} ${record.lastName}",
                                color = dialogColors.borderLight,
                                fontSize = 16.sp
                            )
                            VengText(
                                text = "Возраст: ${record.age}",
                                color = dialogColors.borderLight,
                                fontSize = 12.sp
                            )
                            VengButton(
                                onClick = {
                                    // Загружаем персону по personId через ViewModel
                                    scope.launch {
                                        val persons = policeViewModel.allPersons.value
                                        val person = persons.find { it.id == record.personId }
                                        person?.let { onRecordSelected(it) }
                                    }
                                },
                                text = "Открыть личное дело",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                theme = theme
                            )
                        }
                    } ?: run {
                        VengText(
                            text = "Для отпечатка №$number личное дело не найдено",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(dialogColors.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                VengButton(
                    onClick = onDismiss,
                    text = "Закрыть",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )
            }
        }
    }
}

