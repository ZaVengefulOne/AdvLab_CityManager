package org.vengeful.citymanager.uikit.composables.person

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField

@Composable
fun PersonDialog(
    onDismiss: () -> Unit,
    onAddPerson: (Person) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedRights by remember { mutableStateOf(emptySet<Rights>()) }
    var id by remember { mutableStateOf("") }

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
                .width(400.dp)
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
                            colors = listOf(
                                dialogColors.surface,
                                dialogColors.background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                VengText(
                    text = "ДОБАВИТЬ ЧЕЛОВЕКА",
                    color = dialogColors.borderLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Поля ввода
                VengTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = "ИДЕНТИФИКАТОР",
                    placeholder = "Введите ID...",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                VengTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "ИМЯ",
                    placeholder = "Введите имя...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                VengTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "ФАМИЛИЯ",
                    placeholder = "Введите фамилию...",
                    modifier = Modifier.fillMaxWidth(),
                    theme = theme
                )

                Spacer(modifier = Modifier.height(12.dp))

                SteampunkRightsMultiSelect(
                    selectedRights = selectedRights,
                    onRightsSelected = { selectedRights = it },
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки действий
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VengButton(
                        onClick = onDismiss,
                        text = "ОТМЕНА",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        padding = 12.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = {
                            val personId = id.toIntOrNull() ?: 0
                            if (personId > 0 && firstName.isNotBlank() && lastName.isNotBlank()) {
                                val person = Person(
                                    id = personId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    rights = selectedRights.toList()
                                )
                                onAddPerson(person)
                                onDismiss()
                            }
                        },
                        text = "ДОБАВИТЬ",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        padding = 12.dp,
                        enabled = id.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank() && selectedRights.isNotEmpty(),
                        theme = theme
                    )
                }
            }
        }
    }
}


@Composable
fun SteampunkRightsMultiSelect(
    selectedRights: Set<Rights>,
    onRightsSelected: (Set<Rights>) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val colors = remember(theme) {
        SeveritepunkThemes.getTextFieldColors(theme)
    }

    Column {
        VengText(
            text = "ПРАВА ДОСТУПА:",
            color = colors.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, colors.borderLight, RoundedCornerShape(6.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Rights.entries.forEach { right ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newRights = if (selectedRights.contains(right)) {
                                selectedRights - right
                            } else {
                                selectedRights + right
                            }
                            onRightsSelected(newRights)
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, colors.borderLight, RoundedCornerShape(4.dp))
                            .background(
                                if (selectedRights.contains(right)) colors.borderLight else Color.Transparent
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    VengText(
                        text = right.name,
                        color = colors.text,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteampunkRightsDropdown(
    selectedRights: Rights,
    onRightsSelected: (Rights) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = remember(theme) {
        SeveritepunkThemes.getTextFieldColors(theme)
    }

    Box {
        VengTextField(
            value = selectedRights.name,
            onValueChange = { },
            label = "ПРАВА ДОСТУПА",
            placeholder = "Выберите права...",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            theme = theme
        )

        // Кастомная стрелка
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp, top = 20.dp)
                .clickable { expanded = true }
        ) {
            VengText(
                text = if (expanded) "▲" else "▼",
                color = colors.text,
                fontSize = 12.sp
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.background)
                .border(2.dp, colors.borderLight, RoundedCornerShape(6.dp))
        ) {
            Rights.entries.forEach { right ->
                DropdownMenuItem(
                    onClick = {
                        onRightsSelected(right)
                        expanded = false
                    },
                    modifier = Modifier.background(colors.background),
                    text = {
                        VengText(
                            text = right.name,
                            color = colors.text,
                            fontWeight = FontWeight.Medium
                        )
                    }
                )
            }
        }
    }
}
