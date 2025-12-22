package org.vengeful.citymanager.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import org.vengeful.citymanager.ROUTE_NEWS_ITEM
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.news.NewsSource
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.news.NewsCard
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun NewsScreen(navController: NavController) {
    val viewModel: NewsViewModel = koinViewModel()
    val news by viewModel.news.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTheme = remember { LocalTheme }

    val cardColors = remember(currentTheme) {
        SeveritepunkThemes.getCardColors(currentTheme)
    }
    val colorScheme = remember(currentTheme) {
        SeveritepunkThemes.getColorScheme(currentTheme)
    }

    // Разделение новостей по источникам
    val publishingHouseNews = remember(news) {
        news.filter { it.source == NewsSource.PUBLISHING_HOUSE }
    }
    val ebonyBayNews = remember(news) {
        news.filter { it.source == NewsSource.EBONY_BAY }
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
                    text = "📰 НОВОСТИ ЛЭБТАУНА",
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

            if (isLoading && news.isEmpty()) {
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
            } else if (news.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = "Новостей пока нет...",
                        modifier = Modifier.padding(32.dp),
                        fontSize = 18.sp,
                        color = colorScheme.borderLight.copy(alpha = 0.7f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Колонка "Новость из Издательства"
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 16.dp)
                    ) {
                        // Заголовок колонки
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            VengText(
                                text = "📚 НОВОСТИ ИЗ ИЗДАТЕЛЬСТВА",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardColors.accent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (publishingHouseNews.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                VengText(
                                    text = "Новостей пока нет...",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 14.sp,
                                    color = colorScheme.borderLight.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(1),
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(publishingHouseNews) { newsItem ->
                                    NewsCard(
                                        news = newsItem,
                                        theme = currentTheme,
                                        onCardClick = {
                                            navController.navigate("$ROUTE_NEWS_ITEM/${newsItem.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Вертикальная разделительная линия
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Cyan.copy(alpha = 0.5f),
                                        cardColors.borderLight.copy(alpha = 0.7f),
                                        Color.Cyan.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Колонка "Новости Эбони-Бея"
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 16.dp)
                    ) {
                        // Заголовок колонки
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            VengText(
                                text = "🏛️ВЕСТНИК ЭБОНИ-БЕЯ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardColors.accent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (ebonyBayNews.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                VengText(
                                    text = "Новостей пока нет...",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 14.sp,
                                    color = colorScheme.borderLight.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(1),
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(ebonyBayNews) { newsItem ->
                                    NewsCard(
                                        news = newsItem,
                                        theme = currentTheme,
                                        onCardClick = {
                                            navController.navigate("$ROUTE_NEWS_ITEM/${newsItem.id}")
                                        }
                                    )
                                }
                            }
                        }
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
