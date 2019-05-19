package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import net.capellari.julien.threed.annotations.kotlinwriter.Code
import net.capellari.julien.threed.annotations.kotlinwriter.Function
import net.capellari.julien.threed.annotations.kotlinwriter.KotlinMarker
import net.capellari.julien.threed.annotations.kotlinwriter.Property
import kotlin.reflect.KClass

@KotlinMarker
abstract class AbsType(val builder: TypeSpec.Builder) {
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

    // - propriétés
    fun addProperty(name: String, type: TypeName, build: Property.() -> Unit) {
        builder.addProperty(Property(name, type).apply(build).spec)
    }
    fun addProperty(name: String, type: KClass<*>, build: Property.() -> Unit) {
        builder.addProperty(Property(name, type).apply(build).spec)
    }
    fun addProperty(name: String, type: java.lang.reflect.Type, build: Property.() -> Unit) {
        builder.addProperty(Property(name, type).apply(build).spec)
    }
}