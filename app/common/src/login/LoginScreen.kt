package ktor.chat.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktor.chat.vm.ChatViewModel
import ktor.chat.components.ErrorText
import ktor.chat.utils.tryRequest
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoginScreen(vm: ChatViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var server by remember { vm.server }
    var serverAvailable by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loadingState = remember { mutableStateOf(false) }
    var loading by loadingState
    var tabIndex by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    var isFullyScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        isFullyScrolled = scrollState.value >= (scrollState.maxValue - 10)
    }

    LaunchedEffect(server) {
        while(true) {
            serverAvailable = vm.isServerAvailable(server)
            delay(1.5.seconds)
        }
    }
    
    @Composable
    fun ifRegistering(block: @Composable () -> Unit) =
        if (tabIndex == 1) block() else {}

    fun login() {
        coroutineScope.tryRequest(loadingState, { error = it }) {
            vm.login(server, email, password)
        }
    }
    
    fun register() {
        if (password != passwordRepeat)
            error = "Passwords do not match"
        else {
            coroutineScope.tryRequest(loadingState, { error = it }) {
                vm.register(server, email, name, password)
            }
        }
    }
    
    WelcomeView {
        FormColumn {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(text = { Text("Login") }, selected = tabIndex == 0, onClick = { tabIndex = 0 })
                Tab(text = { Text("Register") }, selected = tabIndex == 1, onClick = { tabIndex = 1 })
            }
            Column(
                Modifier.fillMaxWidth().height(225.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TextField(
                    server,
                    { server = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Server")
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Server status",
                            tint = if (serverAvailable) Color(40, 220, 60) else Color(220, 40, 40),
                            modifier = Modifier.size(ButtonDefaults.IconSize * 0.75f)
                        )
                    }
                )
                TextField(
                    email,
                    { email = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Email")
                    }
                )
                ifRegistering {
                    TextField(
                        name,
                        { name = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Name")
                        }
                    )
                }
                TextField(
                    password,
                    { password = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    label = {
                        Text("Password")
                    }
                )
                ifRegistering {
                    TextField(
                        passwordRepeat,
                        { passwordRepeat = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        label = {
                            Text("Repeat password")
                        }
                    )
                }
            }
            
            error?.let {
                ErrorText(it, modifier = Modifier.align(Alignment.End))
            }

            if (isFullyScrolled) {
                when (tabIndex) {
                    0 -> Button(
                        ::login,
                        modifier = Modifier.align(Alignment.End),
                        enabled = !loading && sequenceOf(server, email, password).all { it.isNotBlank() }
                    ) {
                        Text(if (loading) "Submitting..." else "Login")
                    }

                    1 -> Button(
                        ::register,
                        modifier = Modifier.align(Alignment.End),
                        enabled = !loading && sequenceOf(
                            server,
                            email,
                            name,
                            password,
                            passwordRepeat
                        ).all { it.isNotBlank() }
                    ) {
                        Text(if (loading) "Submitting..." else "Register")
                    }
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("More...")
                }

            }
        }
    }
}