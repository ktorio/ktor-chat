package io.ktor.chat

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Users : LongIdTable() {
    val name = varchar("name", length = 42)
    val email = varchar("email", length = 128).uniqueIndex()
    val password = varchar("password", length = 128)
}

object Rooms : LongIdTable() {
    val name: Column<String> = varchar("name", length = 42).uniqueIndex()
}

object Messages : LongIdTable() {
    val author = reference("author", Users, onDelete = ReferenceOption.CASCADE)
    val created = timestamp("created")
    val room = long("room").references(Rooms.id, onDelete = ReferenceOption.CASCADE)
    val text = text("text")
    val modified = timestamp("modified").nullable()
}

object Members : LongIdTable() {
    val user = reference("user", Users, onDelete = ReferenceOption.CASCADE)
    val room = reference("room", Rooms, onDelete = ReferenceOption.CASCADE)
}