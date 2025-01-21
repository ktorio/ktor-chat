package io.ktor.di

import io.ktor.server.application.createApplicationPlugin
import io.ktor.util.reflect.TypeInfo

// TODO configure provider, resolver, etc.
val DI = createApplicationPlugin(name = "DI", ::DIConfig) {
    application.attributes.put(
        DependencyRegistryKey,
        DefaultDependencyRegistry(DependencyReflection.Default)
    )
}

class DIConfig {
    /**
     * Default implementation.
     * Can be used for delegation or as a fallback.
     */
    val default: DependencyRegistry = DefaultDependencyRegistry(DependencyReflection.Default)

    var reflection: DependencyReflection? = null
    var provider: DependencyProvider? = null
    var resolver: DependencyResolver? = null
}

/**
 * Unique key for a dependency.
 */
data class DependencyKey(val type: TypeInfo, val name: String? = null) {
    override fun toString(): String = buildString {
        append('(')
        append(type.toString())
        if (name != null) {
            append(", \"$name\"")
        }
        append(')')
    }
}

class MissingDependencyException(key: DependencyKey) :
    IllegalArgumentException("Could not resolve dependency for $key")

// TODO show loop for traceability
class CircularDependencyException(key: DependencyKey) :
    IllegalStateException("Circular dependency for $key")

class OutOfOrderDependencyException(key: DependencyKey) :
    IllegalStateException("Attempted to define $key after dependencies were resolved")

class DuplicateDependencyException(key: DependencyKey) :
    IllegalArgumentException("Attempted to define $key after dependencies were resolved")