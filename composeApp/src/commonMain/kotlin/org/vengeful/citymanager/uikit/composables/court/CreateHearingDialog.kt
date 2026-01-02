package org.vengeful.citymanager.uikit.composables.court

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.court.Hearing
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CreateHearingDialog(
    cases: List<Case>,
    onDismiss: () -> Unit,
    onCreateHearing: (Hearing) -> Unit,
    onUpdateCaseStatus: (Int, CaseStatus) -> Unit,
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    val personInteractor: IPersonInteractor = remember { GlobalContext.get().get() }
    val scope = rememberCoroutineScope()

    var hearingId by remember { mutableIntStateOf(0) }
    var selectedCase by remember { mutableStateOf<Case?>(null) }
    var selectedCaseId by remember { mutableIntStateOf(-1) }
    var plaintiffPersonId by remember { mutableStateOf<Int?>(null) }
    var plaintiffName by remember { mutableStateOf("") }
    var isCityPlaintiff by remember { mutableStateOf(false) }
    var plaintiffDropdownExpanded by remember { mutableStateOf(false) }
    var plaintiffSearchQuery by remember { mutableStateOf("") }
    var caseDropdownExpanded by remember { mutableStateOf(false) }
    var caseSearchQuery by remember { mutableStateOf("") }

    // Auto-filled fields from case
    var suspectName by remember { mutableStateOf("") }
    var investigatorName by remember { mutableStateOf("") }
    var violationArticle by remember { mutableStateOf("") }
    var statementText by remember { mutableStateOf("") }

    // Stage 2 fields
    var isEditMode by remember { mutableStateOf(false) }
    var protocol by remember { mutableStateOf("") }
    var verdict by remember { mutableStateOf("") }

    var persons by remember { mutableStateOf<List<Person>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                persons = personInteractor.getPersons()
            } catch (e: Exception) {
                println("Failed to load persons: ${e.message}")
            }
        }
    }

    // Auto-fill when case is selected
    LaunchedEffect(selectedCaseId) {
        if (selectedCaseId > 0) {
            val case = cases.find { it.id == selectedCaseId }
            if (case != null) {
                selectedCase = case
                suspectName = case.suspectName
                violationArticle = case.violationArticle
                statementText = case.statementText

                scope.launch {
                    val investigator = personInteractor.getPersonById(case.investigatorPersonId)
                    investigatorName = investigator?.let { "${it.firstName} ${it.lastName}" } ?: "Неизвестно"
                }
            }
        }
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

    val textFieldColors = remember(theme) {
        SeveritepunkThemes.getTextFieldColors(theme)
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
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    VengText(
                        text = if (isEditMode) "Создание слушания - Протокол и вердикт" else "Создание слушания",
                        color = dialogColors.borderLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // ID (auto-generated, read-only)
                    VengText(
                        text = "ID слушания: ${if (hearingId > 0) hearingId else "Будет присвоен автоматически"}",
                        color = dialogColors.borderLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Выбор дела
                    Box {
                        val selectedCaseDisplay = cases.find { it.id == selectedCaseId }
                        VengTextField(
                            value = selectedCaseDisplay?.let { "Дело №${it.id} - ${it.suspectName}" }
                                ?: "Выберите дело...",
                            onValueChange = { },
                            label = "Дело (со статусом 'Передано в суд')",
                            placeholder = "Нажмите для выбора...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (!isEditMode) caseDropdownExpanded = true },
                            enabled = !isEditMode,
                            theme = theme
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp, top = 20.dp)
                                .clickable { if (!isEditMode) caseDropdownExpanded = true }
                        ) {
                            VengText(
                                text = if (caseDropdownExpanded) "▲" else "▼",
                                color = textFieldColors.text,
                                fontSize = 12.sp
                            )
                        }

                        DropdownMenu(
                            expanded = caseDropdownExpanded && !isEditMode,
                            onDismissRequest = {
                                caseDropdownExpanded = false
                                caseSearchQuery = ""
                            },
                            modifier = Modifier
                                .background(textFieldColors.background)
                                .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                                .width(350.dp)
                        ) {
                            VengTextField(
                                value = caseSearchQuery,
                                onValueChange = { caseSearchQuery = it },
                                label = "Поиск",
                                placeholder = "Введите номер дела или имя подозреваемого...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                theme = theme
                            )
                            val filteredCases = cases.filter { case ->
                                val searchText = caseSearchQuery.lowercase()
                                "${case.id} ${case.suspectName}".lowercase().contains(searchText)
                            }

                            filteredCases.forEach { case ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedCaseId = case.id
                                        caseDropdownExpanded = false
                                        caseSearchQuery = ""
                                    },
                                    modifier = Modifier.background(textFieldColors.background),
                                    text = {
                                        VengText(
                                            text = "Дело №${case.id} - ${case.suspectName}",
                                            color = textFieldColors.text,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Подозреваемый (auto-filled, read-only)
                    VengTextField(
                        value = suspectName,
                        onValueChange = { },
                        label = "Подозреваемый",
                        placeholder = "Автоматически заполняется из дела",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Следователь (auto-filled, read-only)
                    VengTextField(
                        value = investigatorName,
                        onValueChange = { },
                        label = "Следователь",
                        placeholder = "Автоматически заполняется из дела",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Статья правонарушения (auto-filled, read-only)
                    VengTextField(
                        value = violationArticle,
                        onValueChange = { },
                        label = "Статья правонарушения",
                        placeholder = "Автоматически заполняется из дела",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        theme = theme
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Заявление (auto-filled, read-only)
                    VengTextField(
                        value = statementText,
                        onValueChange = { },
                        label = "Заявление",
                        placeholder = "Автоматически заполняется из дела",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        enabled = false,
                        theme = theme,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Истец
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isCityPlaintiff,
                            onCheckedChange = {
                                isCityPlaintiff = it
                                if (it) {
                                    plaintiffPersonId = null
                                    plaintiffName = "Город"
                                    plaintiffDropdownExpanded = false
                                } else {
                                    plaintiffName = ""
                                }
                            },
                            enabled = !isEditMode,
                            colors = CheckboxDefaults.colors(
                                checkedColor = dialogColors.borderLight,
                                uncheckedColor = dialogColors.borderLight.copy(alpha = 0.6f),
                                checkmarkColor = dialogColors.background
                            )
                        )
                        VengText(
                            text = "Город",
                            color = dialogColors.borderLight,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (!isCityPlaintiff && !isEditMode) {
                        Box {
                            val selectedPerson = persons.find { it.id == plaintiffPersonId }
                            VengTextField(
                                value = selectedPerson?.let { "${it.firstName} ${it.lastName} (ID: ${it.id})" }
                                    ?: "Выберите истца...",
                                onValueChange = { },
                                label = "Истец",
                                placeholder = "Нажмите для выбора...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { plaintiffDropdownExpanded = true },
                                enabled = false,
                                theme = theme
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp, top = 20.dp)
                                    .clickable { plaintiffDropdownExpanded = true }
                            ) {
                                VengText(
                                    text = if (plaintiffDropdownExpanded) "▲" else "▼",
                                    color = textFieldColors.text,
                                    fontSize = 12.sp
                                )
                            }

                            DropdownMenu(
                                expanded = plaintiffDropdownExpanded,
                                onDismissRequest = {
                                    plaintiffDropdownExpanded = false
                                    plaintiffSearchQuery = ""
                                },
                                modifier = Modifier
                                    .background(textFieldColors.background)
                                    .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                                    .width(350.dp)
                            ) {
                                VengTextField(
                                    value = plaintiffSearchQuery,
                                    onValueChange = { plaintiffSearchQuery = it },
                                    label = "Поиск",
                                    placeholder = "Введите имя, фамилию или ID...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    theme = theme
                                )
                                val filteredPersons = persons.filter { person ->
                                    val searchText = plaintiffSearchQuery.lowercase()
                                    "${person.firstName} ${person.lastName} ${person.id}".lowercase().contains(searchText)
                                }

                                filteredPersons.forEach { person ->
                                    DropdownMenuItem(
                                        onClick = {
                                            plaintiffPersonId = person.id
                                            plaintiffName = "${person.firstName} ${person.lastName}"
                                            plaintiffDropdownExpanded = false
                                            plaintiffSearchQuery = ""
                                        },
                                        modifier = Modifier.background(textFieldColors.background),
                                        text = {
                                            VengText(
                                                text = "${person.firstName} ${person.lastName} (ID: ${person.id})",
                                                color = textFieldColors.text,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    } else if (isCityPlaintiff) {
                        VengTextField(
                            value = "Город",
                            onValueChange = { },
                            label = "Истец",
                            placeholder = "Город",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            theme = theme
                        )
                    }

                    // Stage 2 fields (only shown after "Готово")
                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(24.dp))

                        VengText(
                            text = "Протокол и вердикт",
                            color = dialogColors.borderLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Протокол (large field)
                        VengTextField(
                            value = protocol,
                            onValueChange = { protocol = it },
                            label = "Протокол",
                            placeholder = "Введите текст протокола слушания...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            theme = theme,
                            maxLines = 15
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Вердикт (small field)
                        VengTextField(
                            value = verdict,
                            onValueChange = { verdict = it },
                            label = "Вердикт",
                            placeholder = "Введите решение суда...",
                            modifier = Modifier.fillMaxWidth(),
                            theme = theme
                        )
                    }
                }

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "Отмена",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            if (!isEditMode) {
                                // Validate stage 1
                                if (selectedCaseId > 0 && plaintiffName.isNotBlank()) {
                                    isEditMode = true
                                }
                            } else {
                                // Save hearing
                                if (protocol.isNotBlank() && verdict.isNotBlank() && selectedCase != null) {
                                    val hearing = Hearing(
                                        caseId = selectedCase!!.id,
                                        plaintiffPersonId = if (isCityPlaintiff) null else plaintiffPersonId,
                                        plaintiffName = plaintiffName,
                                        protocol = protocol,
                                        verdict = verdict,
                                        createdAt = Clock.System.now().toEpochMilliseconds(),
                                        updatedAt = Clock.System.now().toEpochMilliseconds()
                                    )
                                    onCreateHearing(hearing)
                                    onUpdateCaseStatus(selectedCase!!.id, CaseStatus.VERDICT_PRONOUNCED)
                                    onDismiss()
                                }
                            }
                        },
                        text = if (isEditMode) "Сохранить" else "Готово",
                        enabled = if (!isEditMode) {
                            selectedCaseId > 0 && plaintiffName.isNotBlank()
                        } else {
                            protocol.isNotBlank() && verdict.isNotBlank()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}



