package ktor.chat.vm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.chat.*
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * The usual SavedStateHandle isn't available on desktop, so we implement this to persist values to a local JSON file on changes.
 */
@OptIn(ExperimentalSerializationApi::class)
@Composable
actual fun createViewModel(): ChatViewModel {
    val storageFile = Paths.get("state.json")
    val stored: StoredStateModel? = try {
        storageFile.takeIf { it.exists() }?.inputStream()?.use { input ->
            Json.decodeFromStream(input)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    return viewModel {
        ChatViewModel(
            stored?.server ?: "http://localhost:8080",
            stored?.token,
            stored?.loggedInUser,
            stored?.room,
        )
    }.apply {
        LaunchedEffect(Unit) {
            try {
                if (!verify()) logout()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        LaunchedEffect(server.value, token.value, loggedInUser.value, room.value) {
            try {
                saveFile(storageFile, toStorageModel())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

private fun ChatViewModel.toStorageModel() =
    StoredStateModel(
        server.value,
        token.value,
        loggedInUser.value,
        room.value,
    )

@OptIn(ExperimentalSerializationApi::class)
private fun saveFile(file: Path, model: StoredStateModel) {
    try {
        file.outputStream().use { output ->
            Json.encodeToStream(model, output)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

@Serializable
data class StoredStateModel(
    val server: String,
    val token: String?,
    val loggedInUser: User?,
    val room: Membership?,
)