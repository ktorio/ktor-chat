package ktor.chat.client

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.ktor.chat.*
import io.ktor.chat.Nothing
import ktor.chat.utils.Done
import ktor.chat.utils.Remote
import ktor.chat.utils.load

@Composable
inline fun <reified E: Identifiable<ID>, ID> Repository<E, ID>.remoteList(query: Query = Everything): State<Remote<List<E>>> =
    load(query) { list(query) }

@Composable
inline fun <reified E: Identifiable<ID>, ID> ObservableRepository<E, ID>.remoteListWithUpdates(query: Query = Everything): State<Remote<SnapshotStateList<E>>> {
    val remoteList = load(query) { mutableStateListOf(*list(query).toTypedArray()) }
    val predicate = query.toPredicate(E::class)
    
    DisposableEffect(query) {
        val observer = onChange { changeType, e ->
            val currentList = (remoteList.value as? Done ?: return@onChange).value
            if (!predicate(e)) return@onChange
            
            when(changeType) {
                ChangeType.CREATE -> currentList.add(e)
                ChangeType.UPDATE -> currentList.map { if (it.id == e.id) e else it }
                ChangeType.DELETE -> currentList.removeAll { it.id == e.id }
            }
        }
        onDispose {
            forget(observer)
        }
    }
    
    return remoteList
}

@Composable
fun ObservableRepository<Message, Long>.listInRoom(room: Room?): State<Remote<SnapshotStateList<Message>>> =
    remoteListWithUpdates(
        room?.let {
            MapQuery {
                this["room"] = room.id
            }
        } ?: Nothing
    )