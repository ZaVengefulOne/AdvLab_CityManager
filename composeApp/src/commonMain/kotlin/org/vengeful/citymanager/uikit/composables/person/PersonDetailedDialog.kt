package org.vengeful.citymanager.uikit.composables.person

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.veng.VengButton

@Composable
fun PersonDetailedDialog(
    person: Person,
    onDismiss: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val dialogColors = remember(theme) {
        when (theme) {
            ColorTheme.GOLDEN -> DialogColors(
                background = Color(0xFF4A3C2A),
                borderLight = Color(0xFFD4AF37),
                borderDark = Color(0xFF8B7355),
                surface = Color(0xFF5D4A2E)
            )
            ColorTheme.SEVERITE -> DialogColors(
                background = Color(0xFF34495E),
                borderLight = Color(0xFF4A90E2),
                borderDark = Color(0xFF2C3E50),
                surface = Color(0xFF2C3E50)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = dialogColors.background,
            modifier = Modifier
                .width(400.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(dialogColors.borderLight, dialogColors.borderDark)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .shadow(12.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                dialogColors.surface,
                                dialogColors.background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                PersonCard(
                    person = person,
                    modifier = Modifier.fillMaxWidth(),
                    isExpanded = true,
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                VengButton(
                    onClick = onDismiss,
                    text = "ЗАКРЫТЬ",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    padding = 12.dp,
                    theme = theme
                )
            }
        }
    }
}