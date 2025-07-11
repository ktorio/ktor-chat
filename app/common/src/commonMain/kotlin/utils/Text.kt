package io.ktor.chat.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun String.titleCase() =
    if (isEmpty()) this
    else get(0).uppercaseChar() + substring(1).lowercase()

fun TextFieldValue.insert(input: String) =
    copy(
        text = text.substring(0, selection.start) + input + text.substring(selection.end) + input,
        selection = TextRange(selection.end + 1)
    )