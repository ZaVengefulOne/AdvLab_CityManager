package org.vengeful.citymanager.uikit.composables.administration


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.vengeful.citymanager.models.ChatMessage
import org.vengeful.citymanager.uikit.composables.veng.VengButton
import org.vengeful.citymanager.uikit.composables.veng.VengText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminChatWidget(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2C3E50),
    borderColor: Color = Color(0xFF4A90E2)
) {
    var messageText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VengText(
            text = "Связь с Эбони-Беем",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFF1A2530), RoundedCornerShape(4.dp))
                .padding(8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (messages.isEmpty()) {
                Text(
                    text = "Нет сообщений",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            } else {
                messages.forEach { message ->
                    ChatMessageItem(message)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1A2530), RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .height(36.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 14.sp
                ),
                singleLine = true
            )

            VengButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                text = "Отправить",
                modifier = Modifier.height(36.dp),
                padding = 8.dp,
                theme = org.vengeful.citymanager.uikit.ColorTheme.SEVERITE
            )
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VengText(
                text = if (message.sender == "admin") "Эбони-Бэй" else "Лабтаун",
                color = if (message.sender == "admin") Color(0xFF4A90E2) else Color(0xFF27AE60),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            VengText(
                text = timeString,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
        VengText(
            text = message.text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
