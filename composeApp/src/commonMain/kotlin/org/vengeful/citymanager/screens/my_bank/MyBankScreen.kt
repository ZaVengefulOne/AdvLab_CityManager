package org.vengeful.citymanager.screens.my_bank


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme

@Suppress("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBankScreen(navController: NavController) {
    val viewModel: MyBankViewModel = koinViewModel()
    val persons = viewModel.persons.collectAsState().value
    val currentPerson = viewModel.currentPerson.collectAsState().value
    val selectedRecipientId = viewModel.selectedRecipientId.collectAsState().value
    val transferAmount = viewModel.transferAmount.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val transferSuccess = viewModel.transferSuccess.collectAsState().value

    var currentTheme by remember { mutableStateOf(LocalTheme) }
    var recipientDropdownExpanded by remember { mutableStateOf(false) }
    var recipientSearchQuery by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        viewModel.loadPersons()
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VengText(
                    text = "Баланс и переводы",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).borderLight,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                VengButton(
                    onClick = { navController.popBackStack() },
                    text = "Назад",
                    theme = currentTheme,
                )
            }

            if (currentPerson != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = SeveritepunkThemes.getCardColors(currentTheme).let { cardColors ->
                        CardDefaults.cardColors(
                            containerColor = cardColors.background
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VengText(
                            text = "${currentPerson.firstName} ${currentPerson.lastName}",
                            color = SeveritepunkThemes.getColorScheme(currentTheme).text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        VengText(
                            text = "Баланс: ${String.format("%.2f", currentPerson.balance)}",
                            color = if (currentPerson.balance >= 0) Color(0xFF4CAF50) else Color(0xFFFF4444),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                VengText(
                    text = "Войдите для отображения баланса",
                    color = SeveritepunkThemes.getColorScheme(currentTheme).text,
                    fontSize = 16.sp
                )
            }

            // Выбор получателя
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedRecipient = persons.find { it.id == selectedRecipientId }
                VengTextField(
                    value = selectedRecipient?.let { "${it.firstName} ${it.lastName}" } ?: "",
                    onValueChange = { },
                    label = "Получатель",
                    placeholder = "Выберите получателя",
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { recipientDropdownExpanded = true },
                    theme = currentTheme
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, top = 20.dp)
                        .clickable { recipientDropdownExpanded = true }
                ) {
                    VengText(
                        text = if (recipientDropdownExpanded) "▲" else "▼",
                        color = SeveritepunkThemes.getTextFieldColors(currentTheme).text,
                        fontSize = 12.sp
                    )
                }

                DropdownMenu(
                    expanded = recipientDropdownExpanded,
                    onDismissRequest = {
                        recipientDropdownExpanded = false
                        recipientSearchQuery = ""
                    },
                    modifier = Modifier
                        .background(SeveritepunkThemes.getTextFieldColors(currentTheme).background)
                        .border(2.dp, SeveritepunkThemes.getTextFieldColors(currentTheme).borderLight, RoundedCornerShape(6.dp))
                        .width(350.dp)
                ) {
                    VengTextField(
                        value = recipientSearchQuery,
                        onValueChange = { recipientSearchQuery = it },
                        label = "Поиск",
                        placeholder = "Введите имя или фамилию...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        theme = currentTheme
                    )

                    val filteredPersons = persons.filter { person ->
                        if (person.id == currentPerson?.id) return@filter false
                        val searchText = recipientSearchQuery.lowercase()
                        "${person.firstName} ${person.lastName}".lowercase().contains(searchText)
                    }

                    filteredPersons.forEach { person ->
                        DropdownMenuItem(
                            onClick = {
                                viewModel.setSelectedRecipient(person.id)
                                recipientDropdownExpanded = false
                                recipientSearchQuery = ""
                            },
                            modifier = Modifier.background(SeveritepunkThemes.getTextFieldColors(currentTheme).background),
                            text = {
                                VengText(
                                    text = "${person.firstName} ${person.lastName}",
                                    color = SeveritepunkThemes.getTextFieldColors(currentTheme).text,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }

            // Сумма перевода
            VengTextField(
                value = transferAmount,
                onValueChange = { viewModel.setTransferAmount(it) },
                label = "Сумма перевода",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                theme = currentTheme
            )

            // Кнопка перевода
            VengButton(
                onClick = { viewModel.transferMoney() },
                text = "Перевести",
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp,
                enabled = !isLoading && selectedRecipientId != null &&
                    transferAmount.toDoubleOrNull() != null &&
                    transferAmount.toDoubleOrNull()!! > 0 &&
                    currentPerson != null,
                theme = currentTheme
            )

            // Сообщения об ошибках
            errorMessage?.let { error ->
                VengText(
                    text = error,
                    color = Color(0xFFFF4444),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Сообщение об успехе
            if (transferSuccess) {
                VengText(
                    text = "Перевод выполнен успешно!",
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearSuccess()
                }
            }
        }
    }
}
