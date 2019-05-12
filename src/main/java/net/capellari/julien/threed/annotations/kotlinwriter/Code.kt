package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.CodeBlock

@KotlinMarker
class Code {
    // Attributs
    val builder = CodeBlock.builder()

    // Propriétés
    val spec get() = builder.build()

    // Méthodes
    fun add(format: String, vararg args: Any) {
        builder.add(format, *args)
    }

    fun addStatement(format: String, vararg args: Any) {
        builder.addStatement(format, *args)
    }

    operator fun String.unaryPlus() {
        builder.addStatement(this)
    }
}