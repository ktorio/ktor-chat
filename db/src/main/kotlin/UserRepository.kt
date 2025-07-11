package io.ktor.chat

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class UserRepository(database: Database): ExposedRepository<FullUser, Long, Users>(database, Users) {
    override fun rowToEntity(row: ResultRow): FullUser =
        FullUser(
            name = row[Users.name],
            email = row[Users.email],
            password = row[Users.password],
            id = row[Users.id].value,
        )

    override fun FullUser.withId(id: Long): FullUser =
        copy(id = id)

    override fun assignColumns(e: FullUser): Users.(UpdateBuilder<*>) -> Unit = {
        it[name] = e.name
        it[email] = e.email
        it[password] = e.password
    }

}