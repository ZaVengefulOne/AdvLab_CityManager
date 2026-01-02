package org.vengeful.citymanager.screens.commonLibrary

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.ROUTE_LIBRARY_ARTICLE
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
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

    val cardColors = remember(currentTheme) {
        SeveritepunkThemes.getCardColors(currentTheme)
    }
    val colorScheme = remember(currentTheme) {
        SeveritepunkThemes.getColorScheme(currentTheme)
    }

    VengBackground(theme = currentTheme) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок с декоративными элементами
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VengText(
                    text = "📚 БИБЛИОТЕКА",
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.borderLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )

                // Декоративная линия под заголовком
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Cyan,
                                    cardColors.borderLight.copy(alpha = 0.8f),
                                    Color.Cyan,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            if (isLoading && articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorScheme.borderLight,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = "Статей пока нет...",
                        modifier = Modifier.padding(32.dp),
                        fontSize = 18.sp,
                        color = colorScheme.borderLight.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 250.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
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

            // Кнопка "Назад"
            VengButton(
                onClick = { navController.popBackStack() },
                text = stringResource(Res.string.back),
                theme = currentTheme,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
