package io.ktor.chat

import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class UserRepositoryTest {

    private lateinit var repository: UserRepository

    private val sampleUser: FullUser = FullUser("Bob Loblaw", "bob@law.blog", "pwd123")

    @BeforeTest
    fun setUp() {
        val database = Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            driver = "org.h2.Driver",
            password = ""
        ).also { db ->
            transaction(db) {
                SchemaUtils.create(Users, Rooms, Messages)
            }
        }
        repository = UserRepository(database)
    }

    @Test
    fun crud() = runTest {
        val bob = repository.create(sampleUser)
        assertEquals(bob, repository.get(bob.id))
        assertEquals(listOf(bob), repository.list())
        repository.update(bob.copy(email = "bob@bobsnewlaw.blog"))
        assertEquals(bob.id, repository.list { it["email"] = "bob@bobsnewlaw.blog" }.first().id)
        repository.delete(bob.id)
        assertTrue(repository.list().isEmpty())
    }

    @Test
    fun nothing() = runTest {
        val userId = repository.create(sampleUser).id
        assertFalse(repository.list(Everything).isEmpty())
        assertTrue(repository.list(Nothing).isEmpty())
        repository.delete(userId)
    }

}