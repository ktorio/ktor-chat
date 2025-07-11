package io.ktor.chat.calls

sealed class CallAction {
    data class ToggleMicrophone(val isEnabled: Boolean) : CallAction()
    data class ToggleCamera(val isEnabled: Boolean) : CallAction()
    data object EndCall : CallAction()
}