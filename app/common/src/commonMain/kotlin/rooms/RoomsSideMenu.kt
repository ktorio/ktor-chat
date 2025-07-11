package io.ktor.chat.rooms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.chat.*
import io.ktor.chat.components.ChatIcons
import io.ktor.chat.utils.Remote

@Composable
fun RoomsMenu(
    asDropdown: Boolean = false,
    joinedRooms: List<Membership>,
    searchRooms: @Composable (String) -> State<Remote<List<Room>>>,
    selectedRoom: Membership?,
    onSelect: (Membership) -> Unit,
    onJoin: suspend (Room) -> Unit,
    onCreate: suspend (String) -> Unit,
    sideMenu: @Composable ColumnScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    var expandedDialog by remember { mutableStateOf<RoomsDialog?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)

    @Composable
    fun RoomsNavigation() {
        Column(
            modifier = Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(8.dp, 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (joinedRooms.isNotEmpty()) {
                joinedRooms.forEach { membership ->
                    NavigationDrawerItem(
                        selected = membership.id == selectedRoom?.id,
                        onClick = { onSelect(membership) },
                        icon = { Icon(imageVector = ChatIcons.Room, contentDescription = null) },
                        label = { Text(membership.room.name) }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(4.dp))
            }
            NavigationDrawerItem(
                selected = false,
                onClick = { expandedDialog = RoomsDialog.CREATE },
                icon = { Icon(imageVector = ChatIcons.CreateRoom, contentDescription = null) },
                label = { Text("Create") }
            )
            NavigationDrawerItem(
                selected = false,
                onClick = { expandedDialog = RoomsDialog.JOIN },
                icon = { Icon(imageVector = ChatIcons.CreateRoom, contentDescription = null) },
                label = { Text("Join") }
            )
            Spacer(Modifier.weight(1f))

            sideMenu()
        }
    }

    if (asDropdown) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    RoomsNavigation()
                }
            },
            content = content
        )
    } else {
        PermanentNavigationDrawer(
            modifier = Modifier.fillMaxSize(),
            drawerContent = {
                PermanentDrawerSheet {
                    RoomsNavigation()
                }
            },
            content = content
        )
    }

    if (expandedDialog == RoomsDialog.CREATE)
        CreateRoomDialog(onCreate) { expandedDialog = null }
    else if (expandedDialog == RoomsDialog.JOIN)
        JoinRoomDialog(joinedRooms, searchRooms, onJoin) { expandedDialog = null }
}

enum class RoomsDialog {
    CREATE,
    JOIN,
}