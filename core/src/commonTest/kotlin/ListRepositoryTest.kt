package io.ktor.chat

import kotlinx.datetime.Clock
import kotlin.test.*
import kotlinx.coroutines.test.*

class ListRepositoryTest {

    private val leonardo = User(1L, "Leonardo")
    private val users = ListRepository(
        leonardo,
        User(2L, "Donatello"),
        User(3L, "Michelangelo"),
        User(4L, "Raphael"),
        copy = { u, id -> u.copy(id = id) }
    )
    private val messages = ListRepository(
        Message(leonardo, 1L, Clock.System.now(), "Cowabunga!"),
        Message(leonardo, 2L, Clock.System.now(), "Pizza time!"),
        copy = { m, id -> m.copy(id = id) }
    )

    @Test
    fun querying() = runTest {
        assertEquals(4, users.list().size)
        assertEquals(1, users.list {
            it["name"] = "Raphael"
        }.size)

        assertEquals(2, messages.list().size)
        assertEquals(1, messages.list {
            it["room"] = "2"
        }.size)
    }

}