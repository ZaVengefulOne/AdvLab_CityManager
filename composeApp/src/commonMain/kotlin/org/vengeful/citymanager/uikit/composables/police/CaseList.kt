package org.vengeful.citymanager.uikit.composables.police

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.vengeful.citymanager.models.police.Case
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.uikit.ColorTheme

@Composable
fun CaseList(
    cases: List<Case>,
    modifier: Modifier = Modifier,
    onCaseClick: (Case) -> Unit = {},
    filterByStatus: CaseStatus? = null,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val filteredCases = if (filterByStatus != null) {
        cases.filter { it.status == filterByStatus }
    } else {
        cases
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(filteredCases) { case ->
            CaseCard(
                case = case,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onCaseClick(case) },
                theme = theme
            )
        }
    }
}



