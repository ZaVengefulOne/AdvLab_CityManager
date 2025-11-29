import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.uikit.ColorTheme
import org.vengeful.citymanager.uikit.SeveritepunkThemes
import org.vengeful.citymanager.uikit.composables.veng.VengText

@Composable
fun VengRightsMultiSelect(
    selectedRights: Set<Rights>,
    onRightsSelected: (Set<Rights>) -> Unit,
    theme: ColorTheme = ColorTheme.GOLDEN
) {
    val colors = remember(theme) {
        SeveritepunkThemes.getTextFieldColors(theme)
    }

    Column {
        VengText(
            text = "ПРАВА ДОСТУПА:",
            color = colors.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, colors.borderLight, RoundedCornerShape(6.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Rights.entries.forEach { right ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newRights = if (selectedRights.contains(right)) {
                                selectedRights - right
                            } else {
                                selectedRights + right
                            }
                            onRightsSelected(newRights)
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, colors.borderLight, RoundedCornerShape(4.dp))
                            .background(
                                if (selectedRights.contains(right)) colors.borderLight else Color.Transparent
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    VengText(
                        text = right.name,
                        color = colors.text,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
