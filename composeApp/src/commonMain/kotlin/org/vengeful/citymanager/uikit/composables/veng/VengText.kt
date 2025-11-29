package org.vengeful.citymanager.uikit.composables.veng
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.uikit.cinzelFontFamily

@Composable
fun VengText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    letterSpacing: TextUnit = 0.4.sp,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fontStyle: FontStyle = FontStyle.Normal,
    lineHeight: TextUnit = 14.sp,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        fontFamily = cinzelFontFamily(fontStyle),
        maxLines = maxLines,
        overflow = overflow,
        lineHeight = lineHeight
    )
}
