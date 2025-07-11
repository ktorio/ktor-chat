package io.ktor.chat.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import io.ktor.chat.components.ErrorText
import io.ktor.chat.style.SecondaryButtonColors
import io.ktor.chat.utils.tryRequest
import io.ktor.chat.vm.ChatViewModel
import io.ktor.chat.vm.Confirmation

@Composable
fun ConfirmationScreen(vm: ChatViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val loadingState = remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val loading by loadingState
    var code by mutableStateOf(vm.confirmationCode)

    fun doConfirmation() {
        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.confirm(code)
        }
    }
    fun logout() {
        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.logout()
        }
    }

    WelcomeView {
        FormColumn {
            Text(
                buildAnnotatedString {
                    append("Please enter the ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("confirmation code")
                    }
                    append(" we sent you:")
                },
                fontSize = 18.sp,
            )
            TextField(
                code,
                { code = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Code")
                }
            )

            error?.let {
                ErrorText(it, modifier = Modifier.align(Alignment.End))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(::logout, enabled = !loading, colors = SecondaryButtonColors) {
                    Text("Cancel")
                }
                Button(
                    ::doConfirmation,
                    enabled = !loading && code.isNotBlank()
                ) {
                    Text(if (loading) "Submitting..." else "Login")
                }
            }
        }
    }
}

private val ChatViewModel.confirmationCode: String get() =
    (confirmation.value as? Confirmation.Pending)?.code.orEmpty()