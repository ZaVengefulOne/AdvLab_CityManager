package org.vengeful.citymanager.screens.niis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.ROUTE_NIIS_CLEANING
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun NIISMainScreen(navController: NavController) {
    val currentTheme = LocalTheme
    var showSampleDialog by remember { mutableStateOf(false) }
    var sampleNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    VengBackground(theme = currentTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VengText(
                    text = "НИИС",
                    fontSize = 48.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                VengText(
                    text = "Научно Исследовательский \n Институт Северита",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp),
                    lineHeight = 24.sp,
                    maxLines = 2
                )
            }

            Column {
                VengButton(
                    onClick = { showSampleDialog = true },
                    text = "Начать процесс очистки северита",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )

                VengButton(
                    onClick = { navController.popBackStack() },
                    text = "Назад",
                    theme = currentTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
            }
        }
    }

    // Диалог ввода номера образца
    if (showSampleDialog) {
        AlertDialog(
            onDismissRequest = {
                showSampleDialog = false
                sampleNumber = ""
                errorMessage = null
            },
            title = {
                VengText(
                    text = "Введите номер образца",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VengTextField(
                        value = sampleNumber,
                        onValueChange = { newValue ->
                            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                                sampleNumber = newValue
                                errorMessage = null
                            }
                        },
                        label = "Номер образца (6 цифр)",
                        placeholder = "000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        theme = currentTheme
                    )
                    if (errorMessage != null) {
                        VengText(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                VengButton(
                    onClick = {
                        if (sampleNumber.length == 6) {
                            navController.navigate("$ROUTE_NIIS_CLEANING/$sampleNumber")
                            showSampleDialog = false
                            sampleNumber = ""
                            errorMessage = null
                        } else {
                            errorMessage = "Номер должен состоять из 6 цифр"
                        }
                    },
                    text = "Начать очистку",
                    theme = currentTheme
                )
            },
            dismissButton = {
                VengButton(
                    onClick = {
                        showSampleDialog = false
                        sampleNumber = ""
                        errorMessage = null
                    },
                    text = "Отмена",
                    theme = currentTheme
                )
            },
            containerColor = SeveritepunkThemes.getColorScheme(currentTheme).background
        )
    }
}
