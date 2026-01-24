package io.ktor.chat.utils

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun Instant.shortened(
    now: Instant = Clock.System.now(),
    hideDate: Boolean = true,
): String {
    val delta: Duration = now - this

    // No time zones => we can't compute a wall-clock "HH:mm:ss" or real "yesterday".
    // We switch to a purely duration-based, human-readable relative format.
    return buildString {
        append(delta.humanizeRelative())

        // Keep the parameter for source compatibility; optionally provide a hint when asked.
        if (!hideDate) {
            append(" (epoch +")
            append(this@shortened.epochSeconds)
            append("s)")
        }
    }
}

private fun Duration.humanizeRelative(): String {
    val future = this.isNegative()
    val d = this.absoluteValue

    val text = when {
        d < 45.seconds -> "just now"
        d < 60.minutes -> "${d.inWholeMinutes}m"
        d < 24.hours -> "${d.inWholeHours}h"
        else -> "${d.inWholeDays}d"
    }

    return if (future) "in $text" else "$text ago"
}