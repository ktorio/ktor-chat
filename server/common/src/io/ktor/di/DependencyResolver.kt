package io.ktor.di

import io.ktor.util.reflect.*
import kotlin.reflect.KProperty

interface DependencyResolver {
    val reflection: DependencyReflection

    fun <T: Any> get(key: DependencyKey): T
}

fun DependencyResolver.named(key: String) =
    DependencyResolverContext(this, key)

inline operator fun <reified T> DependencyResolver.getValue(thisRef: Any?, property: KProperty<*>): T =
    resolve()

inline operator fun <reified T> DependencyResolverContext.getValue(thisRef: Any?, property: KProperty<*>): T =
    resolver.resolve(key)

data class DependencyResolverContext(
    val resolver: DependencyResolver,
    val key: String,
)

inline fun <reified T> DependencyResolver.resolve(key: String? = null): T =
    get(DependencyKey(typeInfo<T>(), key))