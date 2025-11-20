package org.vengeful.citymanager.uikit.composables.bank

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme

@Composable
fun BankAccountGrid(
    accounts: List<BankAccount>,
    persons: List<Person>,
    modifier: Modifier = Modifier,
    onAccountClick: (BankAccount) -> Unit = {},
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(accounts) { account ->
            val person = account.personId?.let { personId ->
                persons.find { it.id == personId }
            }

            BankAccountCard(
                account = account,
                person = person,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                onCardClick = { onAccountClick(account) },
                theme = theme
            )
        }
    }
}