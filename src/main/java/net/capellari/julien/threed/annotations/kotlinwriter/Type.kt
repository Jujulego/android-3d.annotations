package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

@KotlinMarker
open class Type(val builder: TypeSpec.Builder) {
    // Propriétés
    val spec get() = builder.build()

    // Méthodes
    // - superclass
    fun superclass(pkg: String, name: String) = superclass(ClassName(pkg, name))
    fun superclass(type: TypeName) { builder.superclass(type) }
    fun superclass(type: KClass<*>) { builder.superclass(type) }
    fun superclass(type: java.lang.reflect.Type) { builder.superclass(type) }

    fun addSuperclassParameter(format: String, vararg args: String) {
        builder.addSuperclassConstructorParameter(format, *args)
    }
    fun addSuperclassParameter(build: Code.() -> Unit) {
        builder.addSuperclassConstructorParameter(Code().apply(build).spec)
    }

    fun addSuperinterface(pkg: String, name: String) = addSuperinterface(ClassName(pkg, name))
    fun addSuperinterface(type: TypeName) { builder.addSuperinterface(type) }
    fun addSuperinterface(type: KClass<*>) { builder.addSuperinterface(type) }
    fun addSuperinterface(type: java.lang.reflect.Type) { builder.addSuperinterface(type) }

    // - fonctions
    fun addFunction(name: String, build: Function.() -> Unit) {
        builder.addFunction(Function(name).apply(build).spec)
    }
}