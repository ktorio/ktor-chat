package io.ktor.chat

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class RoomRepository(database: Database) : ExposedRepository<Room, Long, Rooms>(database, Rooms) {
    override fun rowToEntity(row: ResultRow): Room =
        Room(
            name = row[Rooms.name],
            id = row[Rooms.id].value
        )

    override fun Room.withId(id: Long): Room =
        copy(id = id)

    override fun assignColumns(e: Room): Rooms.(UpdateBuilder<*>) -> Unit = {
        it[name] = e.name
    }

}