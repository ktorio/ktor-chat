package io.ktor.chat.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) = Text(
    message,
    color = MaterialTheme.colorScheme.error,
    modifier = modifier,
    fontWeight = FontWeight.Bold
)