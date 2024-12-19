package io.ktor.di

import io.ktor.util.reflect.*
import io.ktor.utils.io.*

interface DependencyProvider {
    fun <T> set(key: DependencyKey, value: DependencyResolver.() -> T)
}

@KtorDsl
operator fun DependencyProvider.invoke(action: DependencyProviderContext.() -> Unit) {
    DependencyProviderContext(this).action()
}

class DependencyProviderContext(
    private val delegate: DependencyProvider
): DependencyProvider by delegate

inline fun <reified T> DependencyProvider.provide(key: String? = null, noinline provide: DependencyResolver.() -> T) =
    set(DependencyKey(typeInfo<T>(), key), provide)