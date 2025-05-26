package io.ktor.chat

fun userRepository(): Repository<FullUser, Long> =
    ListRepository()

fun messagesRepository(): ObservableRepository<Message, Long> =
    ListRepository<Message>().observable()