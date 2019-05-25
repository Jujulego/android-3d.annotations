package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsType

@KotlinMarker
open class Enum: AbsType {
    // Constucteurs
    constructor(name: String): super(TypeSpec.enumBuilder(name))
    constructor(name: ClassName): super(TypeSpec.enumBuilder(name))

    // Méthodes
    fun init(build: Code.() -> Unit) {
        builder.addInitializerBlock(Code().apply(build).spec)
    }

    fun constant(name: String, build: Class.() -> Unit = {}) {
        builder.addEnumConstant(name, Class().apply(build).spec)
    }
}