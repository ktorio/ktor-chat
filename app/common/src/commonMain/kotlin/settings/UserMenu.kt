package io.ktor.chat.settings

import androidx.compose.material.icons.Icons.AutoMirrored.Filled
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import io.ktor.chat.vm.ChatViewModel

@Composable
fun UserMenu(vm: ChatViewModel) {
    val loggedInUser by vm.loggedInUser
    var expanded by remember { mutableStateOf(false) }
    val coroutineContext = rememberCoroutineScope()

    loggedInUser?.let { me ->
        TextButton(onClick = { expanded = !expanded }) {
            Text("Logged in as ${me.name}")
        }
        DropdownMenu(expanded, { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Logout") },
                leadingIcon = { Icon(Filled.Logout, contentDescription = "Logout") },
                onClick = { coroutineContext.launch { vm.logout() } }
            )
        }
    }
}

