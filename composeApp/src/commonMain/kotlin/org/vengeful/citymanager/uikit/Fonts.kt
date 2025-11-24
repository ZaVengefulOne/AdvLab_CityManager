package org.vengeful.citymanager.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font as ResourceFont
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.cinzel_bold
import citymanager.composeapp.generated.resources.cinzel_regular
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.koin.core.component.getScopeId

@Composable
@OptIn(ExperimentalResourceApi::class)
fun cinzelFontFamily(fontStyle: FontStyle): FontFamily {
    return FontFamily(
        Font(Res.font.cinzel_regular, FontWeight.Normal,fontStyle),
        Font(Res.font.cinzel_bold, FontWeight.Bold, fontStyle),
    )
}
