package io.ktor.chat

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.r2dbc.*

class MemberRepository(database: R2dbcDatabase) : ExposedRepository<Membership, ULong, Members>(database, Members) {
    override val tableWithJoins: ColumnSet =
        Members.innerJoin(Users).innerJoin(Rooms)

    override fun rowToEntity(row: ResultRow): Membership =
        Membership(
            id = row[Members.id].value,
            room = Room(
                name = row[Rooms.name],
                id = row[Rooms.id].value,
            ),
            user = User(
                name = row[Users.name],
                id = row[Users.id].value,
            )
        )

    override fun assignColumns(e: Membership): Members.(UpdateBuilder<*>) -> Unit = {
        it[room] = e.room.id
        it[user] = e.user.id
    }

    override fun Membership.withId(id: ULong): Membership =
        copy(id = id)
}