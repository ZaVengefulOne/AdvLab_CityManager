package org.vengeful.citymanager.uikit.composables.library


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.library.Article
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkCardColors
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun ArticleCard(
    article: Article,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val cardColors = remember(theme) {
        SeveritepunkThemes.getCardColors(theme)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardColors.background)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(cardColors.borderLight, cardColors.borderDark)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VengText(
                text = article.title,
                color = cardColors.accent,
                fontSize = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
