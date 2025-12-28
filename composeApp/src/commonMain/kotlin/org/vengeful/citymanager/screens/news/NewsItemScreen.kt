package org.vengeful.citymanager.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
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
import org.vengeful.citymanager.SERVER_BASE_URL
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.news.AsyncNewsImage
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun NewsItemScreen(
    navController: NavController,
    newsId: Int
) {
    val viewModel: NewsItemViewModel = koinViewModel()
    val news by viewModel.news.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTheme = remember { LocalTheme }

    LaunchedEffect(newsId) {
        viewModel.loadNews(newsId)
    }

    val cardColors = remember(currentTheme) {
        SeveritepunkThemes.getCardColors(currentTheme)
    }
    val colorScheme = remember(currentTheme) {
        SeveritepunkThemes.getColorScheme(currentTheme)
    }

    VengBackground(theme = currentTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorScheme.borderLight
                    )
                }
            } else if (news != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VengButton(
                        onClick = { navController.popBackStack() },
                        text = stringResource(Res.string.back),
                        theme = currentTheme,
                        modifier = Modifier.width(128.dp)
                    )

                    VengText(
                        text = news!!.title,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.borderLight,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp,
                        lineHeight = 32.sp
                    )

                    // Невидимый элемент для симметрии
                    Spacer(modifier = Modifier.width(128.dp))
                }

                // Декоративная линия по центру
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(4.dp)
                            .align(Alignment.Center)
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

                // Изображение новости
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    val imageUrl = "$SERVER_BASE_URL${news!!.imageUrl}"
                    AsyncNewsImage(
                        imageUrl = imageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = "Новость не найдена",
                        fontSize = 20.sp,
                        color = colorScheme.borderLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
