package io.ktor.chat

import org.jetbrains.exposed.sql.*

@Suppress("UNCHECKED_CAST")
fun Column<*>.coerce(value: Any): QueryParameter<*> =
    coerce(value, columnType as IColumnType<*>)

private fun coerce(value: Any, columnType: IColumnType<*>): QueryParameter<*> =
    when(val type = columnType) {
        is StringColumnType -> QueryParameter(value.toString(), type as IColumnType<String>)
        is LongColumnType -> QueryParameter(value.toString().toLong(), type as IColumnType<Long>)
        is BooleanColumnType -> QueryParameter(value.toString().toBoolean(), type as IColumnType<Boolean>)
        is IntegerColumnType -> QueryParameter(value.toString().toInt(), type as IColumnType<Int>)
        is EntityIDColumnType<*> -> coerce(value, type.idColumn.columnType as ColumnType<*>)
        is AutoIncColumnType<*> -> coerce(value, type.delegate)
        else -> error("Unsupported column type: ${type::class.simpleName}, $type")
    }