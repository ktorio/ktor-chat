package io.ktor.chat.messages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.chat.*
import io.ktor.chat.utils.shortened

@Composable
fun MessageListItem(message: Message) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp, 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // TODO avatar
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(message.author.name, fontWeight = FontWeight.Bold)
                    Text(message.created.shortened(), color = Color.Gray, fontSize = 12.sp)
                }

                // Text
                Text(message.text)
            }
        }
    }
}