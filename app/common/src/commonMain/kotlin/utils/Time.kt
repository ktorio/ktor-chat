package io.ktor.chat.utils

import kotlinx.datetime.*

fun Instant.shortened(
    now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    hideDate: Boolean = true,
): String {
    val time = toLocalDateTime(TimeZone.currentSystemDefault())
    return when ((now.date - time.date).days) {
        0 -> time.hourMinuteSecond()
        1 -> "yesterday at ${time.hourMinuteSecond()}"
        else -> buildString {
            if (!hideDate)
                append("${time.dayOfMonth} ${time.month.name.titleCase()}, ")
            append(time.hourMinuteSecond())
        }
    }
}


fun LocalDateTime.dateString() =
    "$dayOfMonth ${month.name.titleCase()}"

fun Instant.dateString() =
    toLocalDateTime(TimeZone.currentSystemDefault()).dateString()

fun LocalDateTime.hourMinuteSecond() =
    hour.toString() + ':' + minute.toString().padStart(2, '0') + ':' + second.toString().padStart(2, '0')