package io.ktor.chat.utils

import androidx.compose.runtime.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed interface Remote<T> {
    fun <S> map(mapping: (T) -> S): Remote<S>
}
sealed interface RemoteResult<T>: Remote<T>

class Loading<T>: Remote<T> {
    override fun <S> map(mapping: (T) -> S): Remote<S> = Loading()
}
data class Error<T>(val message: String): RemoteResult<T> {
    override fun <S> map(mapping: (T) -> S): Remote<S> = Error(message)
}
data class Done<T>(val value: T): RemoteResult<T> {
    override fun <S> map(mapping: (T) -> S): Remote<S> = Done(mapping(value))
}

@Composable
inline fun <T> load(vararg keys: Any, crossinline fetch: suspend () -> T): State<Remote<T>> {
    val result = remember { mutableStateOf<Remote<T>>(Loading()) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(*keys) {
        coroutineScope.launch {
            result.value = try {
                Done(fetch())
            } catch (e: Exception) {
                e.printStackTrace()
                Error(e.message ?: "Unknown error!")
            }
        }
    }
    
    return result
}

fun CoroutineScope.tryRequest(
    loading: MutableState<Boolean> = mutableStateOf(false),
    onError: (String) -> Unit = { println(it) },
    request: suspend () -> Unit
) = launch {
    loading.value = true
    try {
        request()
    } catch (e: ClientRequestException) {
        onError(e.response.bodyAsText())
    } catch (e: Exception) {
        onError(e.message ?: "Unexpected error: ${e::class.simpleName}")
    } finally {
        loading.value = false
    }
}