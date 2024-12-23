package io.ktor.di

import io.ktor.util.reflect.TypeInfo
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

interface DependencyReflection {
    companion object {
        val Default = object: DependencyReflection {
            override fun <T: Any> constructors(kClass: KClass<T>): Collection<KFunction<T>> =
                kClass.constructors

            override fun toKey(parameter: KParameter): DependencyKey =
                DependencyKey(TypeInfo(parameter.type.jvmErasure, parameter.type))
        }
    }

    fun <T: Any> create(kClass: KClass<T>, get: (DependencyKey) -> Any): T =
        constructors(kClass).firstNotNullOf { constructor ->
            constructor.callBy(constructor.parameters.associateWith { parameter ->
                get(toKey(parameter))
            })
        }

    fun <T: Any> constructors(kClass: KClass<T>): Collection<KFunction<T>>
    fun toKey(parameter: KParameter): DependencyKey
}

inline fun <reified T: Any> DependencyProvider.provide(kClass: KClass<out T>) =
    provide { create(kClass) }

inline fun <reified T: Any> DependencyResolver.create(): T =
    reflection.create(T::class, ::get)

inline fun <reified T: Any> DependencyResolver.create(kClass: KClass<out T>) =
    reflection.create(kClass, ::get)