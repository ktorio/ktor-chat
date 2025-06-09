package io.ktor.chat

import androidx.compose.runtime.*
import androidx.compose.ui.layout.Layout
import io.ktor.chat.calls.VideoCallScreen
import io.ktor.chat.login.ConfirmationScreen
import io.ktor.chat.login.LoginScreen
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.Confirmation
import io.ktor.chat.vm.VideoCallViewModel
import io.ktor.chat.vm.createViewModel

@Composable
fun ChatApplication(chatVm: ChatViewModel = createViewModel(), videoCallVm: VideoCallViewModel?) {
    val loggedInUser by chatVm.loggedInUser
    val confirmation by chatVm.confirmation
    val isInVideoCall by remember { videoCallVm?.isInVideoCall ?: mutableStateOf(false) }
    var screenSize by remember { chatVm.screenSize }

    LaunchedEffect(loggedInUser) {
        if (loggedInUser == null) {
            return@LaunchedEffect
        }
        videoCallVm?.user = loggedInUser
        videoCallVm?.init(this)
    }

    Layout(
        content = {
            if (confirmation is Confirmation.Pending)
                ConfirmationScreen(chatVm)
            else if (loggedInUser == null)
                LoginScreen(chatVm)
            else if (videoCallVm != null && isInVideoCall)
                VideoCallScreen(videoCallVm, chatVm)
            else
                ChatScreen(chatVm, videoCallVm)
        },
        // Complicated bit of code to get the window dimensions
        measurePolicy = { measurables, constraints ->
            // Use the max width and height from the constraints
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            screenSize = Pair(width, height)

            // Measure and place children composables
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            layout(width, height) {
                var yPosition = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(x = 0, y = yPosition)
                    yPosition += placeable.height
                }
            }
        }
    )
}
