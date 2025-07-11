package io.ktor.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.utils.Done
import io.ktor.chat.utils.Error
import io.ktor.chat.utils.Loading
import io.ktor.chat.utils.Remote

@Composable
fun <T> RemoteLoader(
    remote: Remote<T>,
    handleResult: @Composable (T) -> Unit
) {
    when(remote) {
        is Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp).align(Alignment.Center),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        
        }
        is Error -> {
            ErrorText(remote.message)
        }
        is Done -> {
            handleResult(remote.value)
        }
    }
}