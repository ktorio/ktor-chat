package io.ktor.chat

import org.jetbrains.exposed.v1.core.*

@Suppress("UNCHECKED_CAST")
fun Column<*>.coerce(value: Any): QueryParameter<*> =
    coerce(value, columnType as IColumnType<*>)

private fun coerce(value: Any, columnType: IColumnType<*>): QueryParameter<*> =
    when(columnType) {
        is StringColumnType -> QueryParameter(value.toString(), columnType as IColumnType<String>)
        is ULongColumnType -> QueryParameter(value.toString().toULong(), columnType as IColumnType<ULong>)
        is BooleanColumnType -> QueryParameter(value.toString().toBoolean(), columnType as IColumnType<Boolean>)
        is IntegerColumnType -> QueryParameter(value.toString().toInt(), columnType as IColumnType<Int>)
        is EntityIDColumnType<*> -> coerce(value, columnType.idColumn.columnType as ColumnType<*>)
        is AutoIncColumnType<*> -> coerce(value, columnType.delegate)
        else -> error("Unsupported column type: ${columnType::class.simpleName}, $columnType")
    }