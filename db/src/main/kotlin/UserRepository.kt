package io.ktor.chat

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.r2dbc.*

class UserRepository(database: R2dbcDatabase): ExposedRepository<FullUser, ULong, Users>(database, Users) {
    override fun rowToEntity(row: ResultRow): FullUser =
        FullUser(
            name = row[Users.name],
            email = row[Users.email],
            password = row[Users.password],
            id = row[Users.id].value,
        )

    override fun FullUser.withId(id: ULong): FullUser =
        copy(id = id)

    override fun assignColumns(e: FullUser): Users.(UpdateBuilder<*>) -> Unit = {
        it[name] = e.name
        it[email] = e.email
        it[password] = e.password
    }

}