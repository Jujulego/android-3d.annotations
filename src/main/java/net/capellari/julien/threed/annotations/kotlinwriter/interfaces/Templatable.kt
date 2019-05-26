package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.TypeVariableName
import net.capellari.julien.threed.annotations.kotlinwriter.TypeParameter

interface Templatable {
    // Méthodes
    // - type parameters
    fun typeParameter(name: String, build: TypeParameter.() -> Unit = {}): TypeVariableName
}