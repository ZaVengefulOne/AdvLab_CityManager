package org.vengeful.citymanager.screens.commonLibrary


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengBackground
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.utilities.LocalTheme

@Composable
fun ArticleScreen(
    navController: NavController,
    articleId: Int
) {
    val viewModel: ArticleViewModel = koinViewModel()
    val article by viewModel.article.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTheme = remember { LocalTheme }

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
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
            // Заголовок с кнопкой назад
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
                    modifier = Modifier.weight(0.2f)
                )

                Spacer(modifier = Modifier.weight(0.6f))
            }

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
            } else if (article != null) {
                // Заголовок статьи
                VengText(
                    text = article!!.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.borderLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    lineHeight = 36.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .padding(vertical = 16.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    cardColors.borderLight.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Контент статьи
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    VengText(
                        text = article!!.content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 28.sp,
                        letterSpacing = 0.2.sp,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Visible
                    )
                }
            } else {
                // Сообщение об ошибке или пустом состоянии
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    VengText(
                        text = "Статья не найдена",
                        fontSize = 20.sp,
                        color = colorScheme.borderLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
