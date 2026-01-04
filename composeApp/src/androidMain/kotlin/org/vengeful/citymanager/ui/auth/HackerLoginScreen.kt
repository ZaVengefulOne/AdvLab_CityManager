package org.vengeful.citymanager.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme
import androidx.compose.runtime.collectAsState

@Composable
fun HackerLoginScreen(
    onHackSuccess: () -> Unit,
    theme: ColorTheme = LocalTheme
) {
    val viewModel: HackerLoginViewModel = koinViewModel()

    val code by viewModel.code.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isHacked by viewModel.isHacked.collectAsState()
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val hint by viewModel.hint.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val colors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }

    // Animated cursor
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    // When hacked successfully, call callback
    LaunchedEffect(isHacked) {
        if (isHacked) {
            kotlinx.coroutines.delay(1000)
            onHackSuccess()
        }
    }

    VengBackground(
        theme = theme,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding() // Отступы при открытии клавиатуры
                .navigationBarsPadding(), // Отступы для системных кнопок
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Terminal header
            Text(
                text = "СИСТЕМА БЕЗОПАСНОСТИ СГК",
                color = colors.borderLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Terminal output
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(2.dp, colors.borderLight.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    terminalOutput.forEach { line ->
                        Text(
                            text = line,
                            color = colors.borderLight,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (isTyping) {
                        Text(
                            text = "> Обработка...",
                            color = colors.borderLight.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // Input field (стилизовано под терминал)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = ">",
                    color = colors.borderLight,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Терминальное поле ввода
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            colors.borderLight.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = userInput,
                        onValueChange = { viewModel.updateInput(it) },
                        enabled = !isLocked && !isHacked,
                        textStyle = TextStyle(
                            color = colors.borderLight,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (userInput.length == 6) {
                                    viewModel.submitCode()
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        cursorBrush = SolidColor(colors.borderLight),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                innerTextField()
                                // Cursor
                                if (!isLocked && !isHacked && userInput.length < 6) {
                                    Text(
                                        text = "_",
                                        color = colors.borderLight.copy(alpha = cursorAlpha),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Подсказка (показывается после 3 попыток)
            if (hint != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            colors.borderLight.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "> Подобранный код: $hint",
                        color = colors.borderLight.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Status info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isLocked) "Система заблокирована..."
                           else if (isHacked) "Доступ получен!"
                           else "Попытки: $attempts/${HackerLoginViewModel.MAX_ATTEMPTS}",
                    color = when {
                        isHacked -> Color(0xFF4CAF50)
                        isLocked -> Color(0xFFFF0000)
                        else -> colors.borderLight.copy(alpha = 0.7f)
                    },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Код: ${"*".repeat(6)}",
//                    text = "Код: ${viewModel.code.collectAsState().value}",
                    color = colors.borderLight.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Submit button
            VengButton(
                onClick = { viewModel.submitCode() },
                text = "Ввести",
                modifier = Modifier.fillMaxWidth(),
                padding = 12.dp,
                enabled = !isLocked && !isHacked && userInput.length == 6,
                theme = theme
            )

            if (isTyping) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.borderLight
                )
            }
        }
    }
}

