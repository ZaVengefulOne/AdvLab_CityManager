package org.vengeful.citymanager.screens.administration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.KoinInjector
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.animations.RestartAnimation
import org.vengeful.citymanager.uikit.animations.ShutdownAnimation
import org.vengeful.citymanager.uikit.composables.misc.ThemeSwitcher
import org.vengeful.citymanager.uikit.composables.person.PersonDetailedDialog
import org.vengeful.citymanager.uikit.composables.person.PersonDialog
import org.vengeful.citymanager.uikit.composables.person.PersonsGrid
import org.vengeful.citymanager.uikit.composables.terminal.TerminalControls
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme
import kotlin.system.exitProcess

@Composable
fun AdministrationScreen(navController: NavController) {
    val administrationViewModel = KoinInjector.administrationViewModel
    val persons = administrationViewModel.persons.collectAsState().value
    val curPerson = administrationViewModel.curPerson.collectAsState().value

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    val getId = remember { mutableStateOf("") }
    val delId = remember { mutableStateOf("") }

    // Состояния для анимаций
    var showShutdownAnimation by remember { mutableStateOf(false) }
    var showRestartAnimation by remember { mutableStateOf(false) }

    // Показываем анимации поверх всего
    if (showShutdownAnimation) {
        ShutdownAnimation(
            onComplete = { navController.popBackStack() },
            theme = currentTheme
        )
        return
    }

    if (showRestartAnimation) {
        RestartAnimation(
            onComplete = { showRestartAnimation = false },
            theme = currentTheme
        )
        return
    }

    VengBackground(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        theme = currentTheme,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = "Система Городского Управления v0.0.1",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ThemeSwitcher(
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        LocalTheme = newTheme
                        currentTheme = LocalTheme
                    },
                    modifier = Modifier.width(200.dp)
                )
                TerminalControls(
                    onShutdown = { showShutdownAnimation = true },
                    onRestart = { showRestartAnimation = true },
                    theme = currentTheme
                )
            }

            // ОСНОВНЫЕ ДЕЙСТВИЯ
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Кнопка получения всех
                VengButton(
                    onClick = { administrationViewModel.getPersons() },
                    text = "ПОЛУЧИТЬ ВСЕХ ЖИТЕЛЕЙ",
                    modifier = Modifier.fillMaxWidth(0.8f),
                    theme = currentTheme,
                )

                // Поиск по ID
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ПОИСК ПО ИДЕНТИФИКАТОРУ",
                        color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengTextField(
                            value = getId.value,
                            onValueChange = { getId.value = it },
                            placeholder = "Введите ID...",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            theme = currentTheme,
                        )
                        VengButton(
                            onClick = {
                                if (getId.value.isNotBlank()) {
                                    administrationViewModel.getPersonById(getId.value.toInt())
                                }
                            },
                            text = "НАЙТИ",
                            modifier = Modifier.width(120.dp),
                            enabled = getId.value.isNotBlank(),
                            theme = currentTheme,
                        )
                    }
                }

                // Удаление по ID
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "УДАЛЕНИЕ ПО ИДЕНТИФИКАТОРУ",
                        color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengTextField(
                            value = delId.value,
                            onValueChange = { delId.value = it },
                            placeholder = "Введите ID...",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            theme = currentTheme,
                        )
                        VengButton(
                            onClick = {
                                if (delId.value.isNotBlank()) {
                                    administrationViewModel.deletePerson(delId.value.toInt())
                                }
                            },
                            text = "УДАЛИТЬ",
                            modifier = Modifier.width(120.dp),
                            enabled = delId.value.isNotBlank(),
                            theme = currentTheme,
                        )
                    }
                }

                // Кнопка добавления
                VengButton(
                    onClick = { showAddDialog = true },
                    text = "➕ ДОБАВИТЬ НОВОГО ЖИТЕЛЯ",
                    modifier = Modifier.fillMaxWidth(0.8f),
                    theme = currentTheme,
                )
            }

            // РАЗДЕЛИТЕЛЬ
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFD4AF37),
                                Color.Transparent
                            )
                        )
                    )
            )

            // СЕТКА ЖИТЕЛЕЙ
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "РЕЕСТР ЖИТЕЛЕЙ (${persons.size})",
                        color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (persons.isNotEmpty()) {
                    PersonsGrid(
                        persons = persons,
                        modifier = Modifier.fillMaxSize(),
                        onPersonClick = { person ->
                            selectedPerson = person
                        },
                        theme = currentTheme,
                    )
                } else {
                    // Сообщение о пустой базе
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                SeveritepunkThemes.getColorScheme(currentTheme).borderLight.copy(alpha = 0.2f)
                            )
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗄️ АРХИВ ПУСТ\nЗарегистрируйте первого жителя!",
                            color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }

        // ДИАЛОГ ДОБАВЛЕНИЯ
        if (showAddDialog) {
            PersonDialog(
                onDismiss = { showAddDialog = false },
                onAddPerson = { person ->
                    administrationViewModel.addPerson(person)
                },
                theme = currentTheme,
            )
        }

        // ДИАЛОГ ДЕТАЛЕЙ ПЕРСОНАЖА
        selectedPerson?.let { person ->
            PersonDetailedDialog(
                person = person,
                onDismiss = { selectedPerson = null },
                theme = currentTheme,
            )
        }
    }
}