package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsFunction
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass

@KotlinMarker
class Property {
    // Attributs
    val builder: PropertySpec.Builder

    // Propriétés
    val spec get() = builder.build()

    // Constructeurs
    constructor(name: String, type: TypeName) {
        builder = PropertySpec.builder(name, type)
    }

    constructor(name: String, type: KClass<*>) {
        builder = PropertySpec.builder(name, type)
    }

    constructor(name: String, type: java.lang.reflect.Type) {
        builder = PropertySpec.builder(name, type)
    }

    // Méthodes
    fun addModifiers(vararg modifiers: KModifier) {
        builder.addModifiers(*modifiers)
    }

    fun getter(build: Getter.() -> Unit) {
        builder.getter(Getter().apply(build).spec)
    }

    fun setter(build: Setter.() -> Unit) {
        builder.getter(Setter().apply(build).spec)
    }

    // Classes
    class Getter : AbsFunction(FunSpec.getterBuilder())
    class Setter : AbsFunction(FunSpec.setterBuilder()), Parameters
}