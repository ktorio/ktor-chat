package io.ktor.chat

fun userRepository(): Repository<FullUser, ULong> =
    ListRepository { u, i -> u.copy(id = i) }

fun messagesRepository(): ObservableRepository<Message, ULong> =
    ListRepository<Message> { u, i -> u.copy(id = i) }.observable()