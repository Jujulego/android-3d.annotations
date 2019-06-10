package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsType
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Container
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass

// File
fun createFile(pkg: String, name: String, build: File.() -> Unit) = File(pkg, name).apply(build)

// Type shortcuts
fun<T: Any> KClass<T>.asNullableTypeName(nullable: Boolean = true)
        = asTypeName().copy(nullable = nullable)

inline fun <reified A: Annotation> Annotable.annotation() = annotation(A::class)

inline fun <reified T: Any> File.import(alias: String? = null) = import(T::class, alias)
inline fun <reified T: Any> File.addAlias(name: String, noinline build: TypeAlias.() -> Unit) = addAlias(name, T::class, build)

inline fun <reified T: Any> AbsType.superclass() = superclass(T::class)
inline fun <reified T: Any> AbsType.superinterface() = superinterface(T::class)
inline fun <reified T: Any> AbsType.superinterface(delegate: String, vararg args: Any) = superinterface(T::class, delegate, *args)

inline fun <reified T: Any> TypeParameter.bound(nullable: Boolean = false) = bound(T::class, nullable)

inline fun <reified T: Any> Parameters.parameter(nullable: Boolean = false)
        = parameter(T::class.asNullableTypeName(nullable))

inline fun <reified T: Any> Parameters.parameter(name: String, nullable: Boolean = false, noinline build: Parameter.() -> Unit = {})
        = parameter(name, T::class.asNullableTypeName(nullable), build)

inline fun <reified T: Any> Function.receiver(nullable: Boolean = false)
        = receiver(T::class.asNullableTypeName(nullable))

inline fun <reified T: Any> Returns.returns(nullable: Boolean = false)
        = returns(T::class.asNullableTypeName(nullable))

// Parameters
object unnamed

infix fun String.of(type: TypeName)  = Parameter(this, type)
infix fun String.of(type: KClass<*>) = Parameter(this, type)

infix fun unnamed.of(type: TypeName) = Parameter(type)
infix fun unnamed.of(type: KClass<*>) = Parameter(type)

// Function
inline fun Container.function(name: String, vararg params: Parameter, returns: TypeName? = null, receiver: TypeName? = null, crossinline build: Function.() -> Unit)
        = function(name) {
            receiver?.let { receiver(receiver) }
            params.forEach { builder.addParameter(it.spec) }
            returns?.let { returns(returns) }

            this.build()
        }
inline fun Container.function(name: String, vararg params: Parameter, returns: KClass<*>, receiver: TypeName? = null, crossinline build: Function.() -> Unit)
        = function(name, *params, returns = returns.asTypeName(), receiver = receiver, build = build)
inline fun Container.function(name: String, vararg params: Parameter, returns: TypeName? = null, receiver: KClass<*>, crossinline build: Function.() -> Unit)
        = function(name, *params, returns = returns, receiver = receiver.asTypeName(), build = build)
inline fun Container.function(name: String, vararg params: Parameter, returns: KClass<*>, receiver: KClass<*>, crossinline build: Function.() -> Unit)
        = function(name, *params, returns = returns.asTypeName(), receiver = receiver.asTypeName(), build = build)

// Constructeurs
inline fun Class.primaryConstructor(vararg params: Parameter, crossinline build: Constructor.() -> Unit)
        = primaryConstructor {
            params.forEach { builder.addParameter(it.spec) }

            this.build()
        }
inline fun Class.constructor(vararg params: Parameter, crossinline build: Constructor.() -> Unit)
        = constructor {
            params.forEach { builder.addParameter(it.spec) }

            this.build()
        }

// Property
fun Container.property(name: String, type: KClass<*>, build: Property.() -> Unit = {})
        = property(name, type.asTypeName(), build)

inline fun <reified T: Any> Container.property(name: String, nullable: Boolean = false, noinline build: Property.() -> Unit = {})
        = property(name, T::class.asNullableTypeName(nullable), build)
