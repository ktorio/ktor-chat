package io.ktor.chat

import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class MemberRepository(database: Database) : ExposedRepository<Membership, Long, Members>(database, Members) {
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

    override fun Membership.withId(id: Long): Membership =
        copy(id = id)
}