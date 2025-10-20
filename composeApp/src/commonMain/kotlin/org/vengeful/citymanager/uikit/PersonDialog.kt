package org.vengeful.citymanager.uikit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

@Composable
fun PersonDialog(
    onDismiss: () -> Unit,
    onAddPerson: (Person) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedRights by remember { mutableStateOf(Rights.Any) }
    var id by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Добавить человека",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Поле для ID
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Поле для имени
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Поле для фамилии
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                RightsDropdown(
                        selectedRights = selectedRights,
                        onRightsSelected = { selectedRights = it }
                    )


                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки действий
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val personId = id.toIntOrNull() ?: 0
                            if (personId > 0 && firstName.isNotBlank() && lastName.isNotBlank()) {
                                val person = Person(
                                    id = personId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    rights = selectedRights
                                )
                                onAddPerson(person)
                                onDismiss()
                            }
                        },
                        enabled = id.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank()
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightsDropdown(
    selectedRights: Rights,
    onRightsSelected: (Rights) -> Unit
) {
    val startValue = "Выберите права"
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRights.name,
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Rights.entries.forEach { right ->
                DropdownMenuItem(
                    text = { Text(right.name) },
                    onClick = {
                        onRightsSelected(right)
                        expanded = false
                    }
                )
            }
        }
    }
}