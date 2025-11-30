package org.vengeful.citymanager.uikit.composables.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import citymanager.composeapp.generated.resources.Res
import citymanager.composeapp.generated.resources.coat
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.composables.misc.AccessIndicator
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun ScreenButtonCard(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String,
    hasAccess: Boolean,
    theme: ColorTheme,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AccessIndicator(
            hasAccess = hasAccess,
            theme = theme,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        VengButton(
            onClick = onClick,
            content = icon,
            text = text,
            theme = theme,
            modifier = Modifier.size(100.dp).padding(bottom = 8.dp),
            enabled = hasAccess
        )
        VengText(
            text = text,
        )
    }
}
