package org.vengeful.citymanager.uikit.composables.user

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.uikit.ColorTheme

@Composable
fun UserList(
    users: List<User>,
    modifier: Modifier = Modifier,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    onToggleActive: (User) -> Unit = {},
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        items(users) { user ->
            UserCard(
                user = user,
                modifier = Modifier.fillMaxWidth(),
                onEditClick = { onEditClick(user) },
                onDeleteClick = { onDeleteClick(user) },
                onToggleActive = { onToggleActive(user) },
                theme = theme
            )
        }
    }
}