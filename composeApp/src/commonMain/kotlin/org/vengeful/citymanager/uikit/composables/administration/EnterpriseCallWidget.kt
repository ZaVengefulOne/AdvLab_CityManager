package org.vengeful.citymanager.uikit.composables.administration


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.getDisplayName
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import org.vengeful.citymanager.uikit.composables.veng.VengTextField
import org.vengeful.citymanager.uikit.ColorTheme


@Composable
fun EnterpriseCallWidget(
    onCallEnterprise: (Enterprise) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2C3E50),
    borderColor: Color = Color(0xFF4A90E2),
    theme: ColorTheme = ColorTheme.SEVERITE
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedEnterprise by remember { mutableStateOf<Enterprise?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val textFieldColors = remember(theme) {
        SeveritepunkThemes.getColorScheme(theme)
    }
    val entries = Enterprise.entries.filter{ it != Enterprise.ADMINISTRATION }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(3000)
            showSuccessMessage = false
        }
    }

    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VengText(
            text = "Вызов представителя",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Box {
            VengTextField(
                value = selectedEnterprise?.getDisplayName() ?: "",
                onValueChange = { },
                label = "ВЫБЕРИТЕ ПРЕДПРИЯТИЕ",
                placeholder = "Выберите из списка...",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                theme = theme,
                enabled = true
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp, top = 20.dp)
                    .clickable { expanded = true }
            ) {
                VengText(
                    text = if (expanded) "▲" else "▼",
                    color = textFieldColors.text,
                    fontSize = 12.sp
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(textFieldColors.background)
                    .border(2.dp, textFieldColors.borderLight, RoundedCornerShape(6.dp))
                    .width(350.dp)
            ) {
                entries.forEach { enterprise ->
                    DropdownMenuItem(
                        onClick = {
                            selectedEnterprise = enterprise
                            expanded = false
                        },
                        modifier = Modifier.background(textFieldColors.background),
                        text = {
                            VengText(
                                text = enterprise.getDisplayName(),
                                color = textFieldColors.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        VengButton(
            onClick = {
                selectedEnterprise?.let { enterprise ->
                    onCallEnterprise(enterprise)
                    successMessage = "Представитель ${enterprise.getDisplayName()} успешно вызван"
                    showSuccessMessage = true
                }
            },
            text = "Вызвать",
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedEnterprise != null,
            theme = theme
        )

        if (showSuccessMessage) {
            VengText(
                text = successMessage,
                color = Color(0xFF27AE60),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
