package org.vengeful.citymanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.uikit.composables.dialogs.DeleteConfirmationDialog
import org.vengeful.citymanager.uikit.composables.user.UserEditDialog
import org.vengeful.citymanager.uikit.composables.user.UserList
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme
import org.koin.core.context.GlobalContext

@Composable
fun UsersListScreen(navController: NavController) {
    val viewModel: UsersListViewModel = koinViewModel()
    val personInteractor: IPersonInteractor = remember { GlobalContext.get().get() }

    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var persons by remember { mutableStateOf<List<Person>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
        try {
            persons = personInteractor.getAdminPersons()
        } catch (e: Exception) {
            // Ignore error
        }
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
                text = "Управление пользователями",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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

            if (isLoading && users.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    if (users.isNotEmpty()) {
                        UserList(
                            users = users,
                            modifier = Modifier.fillMaxSize(),
                            onEditClick = { user ->
                                userToEdit = user
                            },
                            onDeleteClick = { user ->
                                userToDelete = user
                            },
                            theme = LocalTheme
                        )
                    } else {
                        VengText(
                            text = "Нет пользователей",
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

    // Диалог редактирования пользователя
    userToEdit?.let { user ->
        UserEditDialog(
            user = user,
            persons = persons,
            onDismiss = { userToEdit = null },
            onSave = { updatedUser, password, personId ->
                viewModel.updateUser(updatedUser, password, personId)
                userToEdit = null
            },
            theme = LocalTheme
        )
    }

    // Диалог подтверждения удаления
    userToDelete?.let { user ->
        DeleteConfirmationDialog(
            onDismiss = { userToDelete = null },
            onConfirm = {
                viewModel.deleteUser(user.id)
                userToDelete = null
            },
            theme = LocalTheme
        )
    }
}

