package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

@KotlinMarker
abstract class _Function(val builder: FunSpec.Builder) {
    // Propriétés
    val spec get() = builder.build()

    // Méthodes
    fun addModifiers(vararg modifiers: KModifier) {
        builder.addModifiers(*modifiers)
    }

    fun addStatement(format: String, vararg args: Any) {
        builder.addStatement(format, *args)
    }

    fun addCode(build: Code.() -> Unit) {
        builder.addCode(Code().apply(build).spec)
    }

    operator fun String.unaryPlus() {
        builder.addStatement(this)
    }
}