package io.ktor.di

import io.ktor.server.application.*
import io.ktor.util.*

val DependencyRegistryKey = AttributeKey<DependencyRegistry>("DependencyRegistry")

interface DependencyRegistry: DependencyProvider, DependencyResolver

var Application.dependencies: DependencyRegistry
    get() {
        if (!attributes.contains(DependencyRegistryKey)) {
            install(DI)
        }
        return attributes[DependencyRegistryKey]
    }
    set(value) {
        attributes.put(DependencyRegistryKey, value)
    }

@Suppress("UNCHECKED_CAST")
class DefaultDependencyRegistry(override val reflection: DependencyReflection): DependencyRegistry {

    private val declarations = mutableMapOf<DependencyKey, DependencyResolver.() -> Any>()
    private val resolved = lazy {
        RegistryInjector().let { injector ->
            declarations.keys.associateWith<DependencyKey, Any>(injector::get)
        }
    }

    override fun <T> set(key: DependencyKey, value: DependencyResolver.() -> T) {
        if (resolved.isInitialized()) throw OutOfOrderDependencyException(key)
        if (declarations.containsKey(key)) throw DuplicateDependencyException(key)
        declarations[key] = value as DependencyResolver.() -> Any
    }

    override fun <T : Any> get(key: DependencyKey): T =
        (resolved.value[key] ?: throw MissingDependencyException(key)) as T

    inner class RegistryInjector: DependencyResolver {
        override val reflection: DependencyReflection
            get() = this@DefaultDependencyRegistry.reflection

        private val resolved = mutableMapOf<DependencyKey, Any>()
        private val visited = mutableSetOf<DependencyKey>()

        override fun <T : Any> get(key: DependencyKey): T =
            resolved.getOrPut(key) {
                if (!visited.add(key)) throw CircularDependencyException(key)
                declarations[key]?.invoke(this) ?: throw MissingDependencyException(key)
            } as T
    }

}