package org.vengeful.citymanager.screens.commonLibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.ROUTE_LIBRARY_ARTICLE
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.composables.library.ArticleCard
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun CommonLibraryScreen(navController: NavController) {
    val viewModel: CommonLibraryViewModel = koinViewModel()
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTheme = remember { LocalTheme }

    VengBackground(theme = currentTheme) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VengText(
                text = "Библиотека",
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading && articles.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            } else if (articles.isEmpty()) {
                VengText(
                    text = "Статей пока нет",
                    modifier = Modifier.padding(32.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 250.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(articles) { article ->
                        ArticleCard(
                            article = article,
                            theme = currentTheme,
                            onCardClick = {
                                navController.navigate("$ROUTE_LIBRARY_ARTICLE/${article.id}")
                            }
                        )
                    }
                }
            }

            VengButton(
                onClick = { navController.popBackStack() },
                text = stringResource(Res.string.back),
                theme = currentTheme,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
