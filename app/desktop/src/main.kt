import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ktor.chat.ChatApplication
import ktor.chat.vm.createViewModel

fun main() = application {
    Window(title = "KTOR CHAT", onCloseRequest = ::exitApplication) {
        ChatApplication(chatVm = createViewModel(), videoCallVm = null)
    }
}