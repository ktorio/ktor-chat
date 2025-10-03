package io.ktor.chat

fun userRepository(): Repository<FullUser, Long> =
    ListRepository { u, i -> u.copy(id = i) }

fun messagesRepository(): ObservableRepository<Message, Long> =
    ListRepository<Message> { u, i -> u.copy(id = i) }.observable()