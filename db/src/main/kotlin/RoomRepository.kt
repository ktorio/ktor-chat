package io.ktor.chat

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.r2dbc.*

class RoomRepository(database: R2dbcDatabase) : ExposedRepository<Room, ULong, Rooms>(database, Rooms) {
    override fun rowToEntity(row: ResultRow): Room =
        Room(
            name = row[Rooms.name],
            id = row[Rooms.id].value
        )

    override fun Room.withId(id: ULong): Room =
        copy(id = id)

    override fun assignColumns(e: Room): Rooms.(UpdateBuilder<*>) -> Unit = {
        it[name] = e.name
    }
}