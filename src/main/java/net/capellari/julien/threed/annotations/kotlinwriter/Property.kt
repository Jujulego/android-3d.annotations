package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
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
}