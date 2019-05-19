package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.asTypeName
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass

// File
fun createFile(pkg: String, name: String, build: File.() -> Unit) = File(pkg, name).apply(build)

// Type shortcut
fun<T: Any> KClass<T>.asNullableTypeName() = asTypeName().copy(nullable = true)

inline fun <reified T: Any> Parameters.addParameter(name: String, nullable: Boolean = false, noinline build: Parameter.() -> Unit = {})
        = addParameter(name, if (nullable) T::class.asNullableTypeName() else T::class.asTypeName(), build)

inline fun <reified T: Any> Returns.returns(nullable: Boolean = false)
        = returns(if (nullable) T::class.asNullableTypeName() else T::class.asTypeName())