package io.ktor.chat

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.datetime.*

object Users : ULongIdTable() {
    val name = varchar("name", length = 42)
    val email = varchar("email", length = 128).uniqueIndex()
    val password = varchar("password", length = 128)
}

object Rooms : ULongIdTable() {
    val name: Column<String> = varchar("name", length = 42).uniqueIndex()
}

object Messages : ULongIdTable() {
    val author = reference("author", Users, onDelete = ReferenceOption.CASCADE)
    val created = timestamp("created")
    val room = ulong("room").references(Rooms.id, onDelete = ReferenceOption.CASCADE)
    val text = text("text")
    val modified = timestamp("modified").nullable()
}

object Members : ULongIdTable() {
    val user = reference("user", Users, onDelete = ReferenceOption.CASCADE)
    val room = reference("room", Rooms, onDelete = ReferenceOption.CASCADE)
}