package io.ktor.chat

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.r2dbc.*

class MessageRepository(database: R2dbcDatabase): ExposedRepository<Message, ULong, Messages>(database, Messages) {

    override val tableWithJoins: ColumnSet
        get() = Messages.innerJoin(Users)

    override fun rowToEntity(row: ResultRow): Message =
        Message(
            author = User(
                name = row[Users.name],
                id = row[Users.id].value,
            ),
            room = row[Messages.room],
            created = row[Messages.created],
            text = row[Messages.text],
            id = row[Messages.id].value,
            modified = row[Messages.modified],
        )

    override fun Message.withId(id: ULong): Message =
        copy(id = id)

    override fun assignColumns(e: Message): Messages.(UpdateBuilder<*>) -> Unit = {
        it[author] = e.author.id
        it[room] = e.room
        it[created] = e.created
        it[text] = e.text
        it[modified] = e.modified
    }

    override suspend fun list(query: Query): List<Message> {
        return super.list(when(query) {
            is MapQuery -> MapQuery.of(query.mapValues { (key, values) ->
                when(key) {
                    "room" -> values.map {  it.toString().toLong() }
                    else -> values
                }
            })
            else -> query
        })
    }
}