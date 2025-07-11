package io.ktor.chat.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: SnapshotStateList<Message>
) {
    val listState = rememberLazyListState()

    // TODO only auto-scroll when already at the bottom
    LaunchedEffect(messages) {
        snapshotFlow { messages.size }
            .distinctUntilChanged()
            .filter { it > 0 }
            .collect { size ->
                listState.animateScrollToItem(index = size - 1)
            }
    }

    when (messages.size) {
        0 -> Box(modifier = modifier.fillMaxSize()) {
            Text("Nothing here yet...", modifier = Modifier.align(Center).padding(10.dp, 5.dp))
        }
        else -> LazyColumn(modifier, state = listState) {
            items(messages) { message ->
                MessageListItem(message)
            }
        }
    }
}