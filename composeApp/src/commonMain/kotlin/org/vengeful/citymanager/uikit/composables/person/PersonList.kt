package org.vengeful.citymanager.uikit.composables.person

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.composables.veng.VengButton

@Composable
fun PersonList(
    persons: List<Person>,
    modifier: Modifier = Modifier,
    onEditClick: (Person) -> Unit,
    onDeleteClick: (Person) -> Unit,
    onPersonClick: ((Person) -> Unit)? = null,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(persons) { person ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PersonCard(
                    person = person,
                    modifier = Modifier.fillMaxWidth(),
                    onCardClick = onPersonClick?.let { { it(person) } },
                    theme = theme
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VengButton(
                        onClick = { onEditClick(person) },
                        text = "Редактировать",
                        modifier = Modifier.weight(1f),
                        padding = 10.dp,
                        theme = theme
                    )

                    VengButton(
                        onClick = { onDeleteClick(person) },
                        text = "Удалить",
                        modifier = Modifier.weight(1f),
                        padding = 10.dp,
                        theme = theme
                    )
                }
            }
        }
    }
}
