package org.vengeful.citymanager.uikit.composables.person

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.vengeful.citymanager.di.koinViewModel
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.police.CaseStatus
import org.vengeful.citymanager.screens.police.CaseViewModel
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.DialogColors
import org.vengeful.citymanager.uikit.composables.police.CaseCard
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun PersonDetailedDialog(
    person: Person,
    onDismiss: () -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val caseViewModel: CaseViewModel = koinViewModel()
    val cases by caseViewModel.cases.collectAsState()
    val casesLoading by caseViewModel.isLoading.collectAsState()

    // Загружаем дела, где житель является подозреваемым
    LaunchedEffect(person.id) {
        caseViewModel.loadCasesBySuspect(person.id)
    }

    val personCases = cases.filter { it.suspectPersonId == person.id }

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
                .width(600.dp)
                .fillMaxWidth(0.9f)
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                PersonCard(
                    person = person,
                    modifier = Modifier.fillMaxWidth(),
                    isExpanded = true,
                    theme = theme
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Секция дел
                if (personCases.isNotEmpty()) {
                    VengText(
                        text = "Дела (подозреваемый): ${personCases.size}",
                        color = dialogColors.borderLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    personCases.forEach { case ->
                        CaseCard(
                            case = case,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            theme = theme
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                } else if (!casesLoading) {
                    VengText(
                        text = "Дела не найдены",
                        color = dialogColors.borderLight.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                VengButton(
                    onClick = onDismiss,
                    text = "Закрыть",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    padding = 12.dp,
                    theme = theme
                )
            }
        }
    }
}
