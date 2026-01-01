package org.vengeful.citymanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import org.vengeful.citymanager.data.bank.IBankInteractor
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.uikit.composables.dialogs.DeleteConfirmationDialog
import org.vengeful.citymanager.uikit.composables.person.PersonList
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.utilities.LocalTheme
import org.koin.core.context.GlobalContext

@Composable
fun PersonsListScreen(navController: NavController) {
    val viewModel: PersonsListViewModel = koinViewModel()
    val bankInteractor: IBankInteractor = remember { GlobalContext.get().get() }

    val persons by viewModel.persons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val cases by viewModel.cases.collectAsState()
    val hearings by viewModel.hearings.collectAsState()

    var personToEdit by remember { mutableStateOf<Person?>(null) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }
    var personForDossier by remember { mutableStateOf<Person?>(null) }
    var bankBalance by remember { mutableStateOf<Double?>(null) }
    var hasBankAccount by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPersons()
        viewModel.loadCasesAndHearings()
    }

    VengBackground(
        theme = LocalTheme,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VengText(
                text = "Управление жителями",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            if (successMessage != null) {
                VengText(
                    text = successMessage!!,
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp
                )
            }

            if (errorMessage != null) {
                VengText(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )
            }

            if (isLoading && persons.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    if (persons.isNotEmpty()) {
                        PersonList(
                            persons = persons,
                            modifier = Modifier.fillMaxSize(),
                            onEditClick = { person ->
                                personToEdit = person
                            },
                            onDeleteClick = { person ->
                                personToDelete = person
                            },
                            onPersonClick = { person ->
                                personForDossier = person
                            },
                            theme = LocalTheme
                        )
                    } else {
                        VengText(
                            text = "Нет жителей",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            VengButton(
                onClick = { navController.navigateUp() },
                text = "Назад",
                theme = LocalTheme,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Диалог редактирования жителя
    personToEdit?.let { person ->
        AndroidPersonEditDialog(
            person = person,
            onDismiss = { personToEdit = null },
            onSave = { updatedPerson ->
                viewModel.updatePerson(updatedPerson)
                personToEdit = null
            },
            theme = LocalTheme
        )
    }

    // Диалог подтверждения удаления
    personToDelete?.let { person ->
        DeleteConfirmationDialog(
            onDismiss = { personToDelete = null },
            onConfirm = {
                viewModel.deletePerson(person.id)
                personToDelete = null
            },
            theme = LocalTheme
        )
    }

    // Диалог досье жителя
    personForDossier?.let { person ->
        val personCases = viewModel.getCasesForPerson(person.id)
        val personHearings = viewModel.getHearingsForPerson(person.id)
        
        LaunchedEffect(person.id) {
            try {
                val account = bankInteractor.getBankAccountByPersonId(person.id)
                hasBankAccount = account != null
                bankBalance = person.balance
            } catch (e: Exception) {
                hasBankAccount = false
                bankBalance = null
            }
        }
        
        AndroidPersonDossierDialog(
            person = person,
            casesAsSuspect = personCases,
            hearings = personHearings,
            bankBalance = bankBalance,
            hasBankAccount = hasBankAccount,
            onDismiss = {
                personForDossier = null
                bankBalance = null
                hasBankAccount = false
            },
            theme = LocalTheme
        )
    }
}

@Composable
private fun AndroidPersonEditDialog(
    person: Person,
    onDismiss: () -> Unit,
    onSave: (Person) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var firstName by remember { mutableStateOf(person.firstName) }
    var lastName by remember { mutableStateOf(person.lastName) }
    var registrationPlace by remember { mutableStateOf(person.registrationPlace) }
    var health by remember { mutableStateOf(person.health) }
    var healthDropdownExpanded by remember { mutableStateOf(false) }
    var balance by remember { mutableStateOf(person.balance.toString()) }

    val healthOptions = remember {
        listOf("здоров", "болен", "критическое состояние", "выздоравливает", "хроническое заболевание")
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                VengText(
                    text = "Редактировать жителя",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                VengTextField(
                    value = person.id.toString(),
                    onValueChange = { },
                    label = "Идентификатор",
                    placeholder = "ID",
                    enabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                VengTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Имя",
                    placeholder = "Введите имя...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                VengTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Фамилия",
                    placeholder = "Введите фамилию...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                VengTextField(
                    value = registrationPlace,
                    onValueChange = { registrationPlace = it },
                    label = "Место регистрации",
                    placeholder = "Введите место регистрации...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Здоровье
                Box {
                    VengTextField(
                        value = health,
                        onValueChange = { },
                        label = "Здоровье",
                        placeholder = "Выберите статус здоровья...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { healthDropdownExpanded = true },
                        enabled = false,
                        theme = theme
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp, top = 20.dp)
                            .clickable { healthDropdownExpanded = true }
                    ) {
                        VengText(
                            text = if (healthDropdownExpanded) "▲" else "▼",
                            fontSize = 12.sp,
                        )
                    }

                    DropdownMenu(
                        expanded = healthDropdownExpanded,
                        onDismissRequest = { healthDropdownExpanded = false },
                        modifier = Modifier
                            .background(dialogColors.surface)
                            .border(2.dp, dialogColors.borderLight, RoundedCornerShape(6.dp))
                            .fillMaxWidth(0.8f)
                    ) {
                        healthOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    health = option
                                    healthDropdownExpanded = false
                                },
                                modifier = Modifier.background(dialogColors.surface),
                                text = {
                                    VengText(
                                        text = option,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Баланс
                VengTextField(
                    value = balance,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            balance = filtered
                        }
                    },
                    label = "Баланс",
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Отмена",
                        modifier = Modifier.weight(1f),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            val balanceValue = try {
                                balance.toDoubleOrNull() ?: 0.0
                            } catch (e: Exception) {
                                0.0
                            }
                            val updatedPerson = Person(
                                id = person.id,
                                firstName = firstName,
                                lastName = lastName,
                                registrationPlace = registrationPlace,
                                health = health,
                                balance = balanceValue,
                                rights = person.rights
                            )
                            onSave(updatedPerson)
                            onDismiss()
                        },
                        text = "Сохранить",
                        modifier = Modifier.weight(1f),
                        padding = 12.dp,
                        enabled = firstName.isNotBlank() && lastName.isNotBlank() && registrationPlace.isNotBlank() && health.isNotBlank(),
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun AndroidPersonDossierDialog(
    person: Person,
    casesAsSuspect: List<Case>,
    hearings: List<Hearing>,
    bankBalance: Double?,
    hasBankAccount: Boolean,
    onDismiss: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
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
                .fillMaxHeight(0.8f)
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
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    VengText(
                        text = "Досье жителя",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    InfoRow(
                        label = "ФИО:",
                        value = "${person.firstName} ${person.lastName}",
                        dialogColors = dialogColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(
                        label = "Место регистрации:",
                        value = person.registrationPlace.ifEmpty { "Не указано" },
                        dialogColors = dialogColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Здоровье
                    val healthStatus = if (person.health == "здоров") "Здоров" else "Болен: ${person.health}"
                    val healthColor = if (person.health == "здоров") Color(0xFF4CAF50) else Color(0xFFF44336)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Здоровье:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = healthStatus,
                            color = healthColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Дела в полиции
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Дела в полиции:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = if (casesAsSuspect.isNotEmpty()) {
                                "Подозреваемый по ${casesAsSuspect.size} дел(у)"
                            } else {
                                "Не проходит"
                            },
                            color = if (casesAsSuspect.isNotEmpty()) Color(0xFFFF9800) else Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (casesAsSuspect.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        casesAsSuspect.forEach { case ->
                            VengText(
                                text = "  • Дело №${case.id}: ${case.violationArticle}",
                                color = dialogColors.borderLight.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Слушания в суде
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Слушания в суде:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = if (hearings.isNotEmpty()) {
                                "Истец по ${hearings.size} слушань(ю/ям)"
                            } else {
                                "Не участвует"
                            },
                            color = if (hearings.isNotEmpty()) Color(0xFFFF9800) else Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (hearings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        hearings.forEach { hearing ->
                            VengText(
                                text = "  • Слушание №${hearing.id}: Дело №${hearing.caseId}",
                                color = dialogColors.borderLight.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                            if (hearing.verdict.isNotBlank()) {
                                VengText(
                                    text = "    Вердикт: ${hearing.verdict}",
                                    color = dialogColors.borderLight.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Банковский счёт
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VengText(
                            text = "Банковский счёт:",
                            color = dialogColors.borderLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        VengText(
                            text = if (hasBankAccount) {
                                "Есть (${String.format("%.2f", bankBalance ?: 0.0)} ₽)"
                            } else {
                                "Нет"
                            },
                            color = if (hasBankAccount) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Закрыть",
                        modifier = Modifier.width(200.dp),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    dialogColors: DialogColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VengText(
            text = label,
            color = dialogColors.borderLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        VengText(
            text = value,
            color = dialogColors.borderLight.copy(alpha = 0.9f),
            fontSize = 14.sp
        )
    }
}

