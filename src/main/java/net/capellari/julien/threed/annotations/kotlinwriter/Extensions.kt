package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.asTypeName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsType
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass

// File
fun createFile(pkg: String, name: String, build: File.() -> Unit) = File(pkg, name).apply(build)

// Type shortcuts
fun<T: Any> KClass<T>.asNullableTypeName(nullable: Boolean = true)
        = asTypeName().copy(nullable = nullable)

inline fun <reified A: Annotation> Annotable<*,*>.annotation()
        = annotation(A::class)

inline fun <reified T: Any> AbsType.superclass(nullable: Boolean = false)
        = superclass(T::class.asNullableTypeName(nullable))

inline fun <reified T: Any> AbsType.superinterface(nullable: Boolean = false)
        = superinterface(T::class.asNullableTypeName(nullable))

inline fun <reified T: Any> AbsType.property(name: String, nullable: Boolean = false, noinline build: Property.() -> Unit = {})
        = property(name, T::class.asNullableTypeName(nullable), build)

inline fun <reified T: Any> Parameters.parameter(name: String, nullable: Boolean = false, noinline build: Parameter.() -> Unit = {})
        = parameter(name, T::class.asNullableTypeName(nullable), build)

inline fun <reified T: Any> Returns.returns(nullable: Boolean = false)
        = returns(T::class.asNullableTypeName(nullable))