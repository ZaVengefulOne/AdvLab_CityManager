package org.vengeful.citymanager.uikit.composables.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme

@Composable
fun PersonsGrid(
    persons: List<Person>,
    modifier: Modifier = Modifier,
    onPersonClick: (Person) -> Unit = {},
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(persons) { person ->
            PersonCard(
                person = person,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                onCardClick = { onPersonClick(person) },
                theme = theme
            )
        }
    }
}
