package org.vengeful.citymanager.ui

import VengRightsMultiSelect
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.data.users.states.RegisterUiState
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.screens.administration.userManagement.UserManagementViewModel
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun UserManagementScreen(navController: NavController) {
    val viewModel: UserManagementViewModel = koinViewModel()

    val persons by viewModel.persons.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRights by remember { mutableStateOf<Set<Rights>>(emptySet()) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var personDropdownExpanded by remember { mutableStateOf(false) }
    var personSearchQuery by remember { mutableStateOf("") }

    val isLoading = registerState is RegisterUiState.Loading
    val isSuccess = registerState is RegisterUiState.Success

    LaunchedEffect(Unit) {
        viewModel.loadPersons()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetRegisterState()
            navController.navigateUp()
        }
    }

    VengBackground(
        theme = LocalTheme,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VengText(
                text = "Создание пользователя",
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            VengTextField(
                value = username,
                onValueChange = { username = it },
                label = "Имя пользователя",
                placeholder = "Введите логин...",
                modifier = Modifier.fillMaxWidth(),
                theme = LocalTheme,
                enabled = !isLoading
            )

            VengTextField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                placeholder = "Введите пароль...",
                modifier = Modifier.fillMaxWidth(),
                theme = LocalTheme,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )
            VengTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Подтверждение пароля",
                placeholder = "Повторите пароль...",
                modifier = Modifier.fillMaxWidth(),
                theme = LocalTheme,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            // Выбор прав
            VengRightsMultiSelect(
                selectedRights = selectedRights,
                onRightsSelected = { selectedRights = it },
                isHacker = true,
                theme = LocalTheme
            )

            // Выбор Person (опционально)
            Box {
                VengTextField(
                    value = selectedPerson?.let { "${it.firstName} ${it.lastName}" } ?: "",
                    onValueChange = { },
                    label = "Выберите человека (опционально)",
                    placeholder = "Выберите из списка...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { personDropdownExpanded = true },
                    theme = LocalTheme,
                    enabled = !isLoading && persons.isNotEmpty()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, top = 20.dp)
                        .clickable { personDropdownExpanded = true }
                ) {
                    Text(
                        text = if (personDropdownExpanded) "▲" else "▼",
                        fontSize = 12.sp
                    )
                }

                DropdownMenu(
                    expanded = personDropdownExpanded,
                    onDismissRequest = {
                        personDropdownExpanded = false
                        personSearchQuery = ""
                    },
                    modifier = Modifier.width(350.dp)
                ) {
                    VengTextField(
                        value = personSearchQuery,
                        onValueChange = { personSearchQuery = it },
                        label = "Поиск",
                        placeholder = "Введите имя или фамилию...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        theme = LocalTheme
                    )
                    val filteredPersons = persons.filter { person ->
                        val searchText = personSearchQuery.lowercase()
                        "${person.firstName} ${person.lastName}".lowercase().contains(searchText)
                    }
                    filteredPersons.forEach { person ->
                        DropdownMenuItem(
                            onClick = {
                                selectedPerson = person
                                personDropdownExpanded = false
                                personSearchQuery = ""
                            },
                            text = {
                                Text(
                                    text = "${person.firstName} ${person.lastName}"
                                )
                            }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isSuccess) {
                Text(
                    text = "Пользователь успешно создан!",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF4CAF50)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VengButton(
                    onClick = {
                        navController.navigateUp()
                        viewModel.resetRegisterState()
                    },
                    text = "Отмена",
                    modifier = Modifier.weight(1f),
                    theme = LocalTheme,
                    enabled = !isLoading
                )

                VengButton(
                    onClick = {
                        if (username.isNotBlank() &&
                            password.isNotBlank() &&
                            password == confirmPassword &&
                            password.length >= 4 &&
                            selectedRights.isNotEmpty() &&
                            !isLoading
                        ) {
                            viewModel.registerUser(
                                username = username,
                                password = password,
                                personId = selectedPerson?.id,
                                rights = selectedRights.toList()
                            )
                        }
                    },
                    text = "СОЗДАТЬ",
                    modifier = Modifier.weight(1f),
                    theme = LocalTheme,
                    enabled = username.isNotBlank() &&
                        password.isNotBlank() &&
                        password == confirmPassword &&
                        password.length >= 4 &&
                        selectedRights.isNotEmpty() &&
                        !isLoading
                )
            }
        }
    }
}
