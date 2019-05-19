package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsType

class Class: AbsType {
    // Constructors
    constructor(name: ClassName): super(TypeSpec.classBuilder(name))
    constructor(name: String): super(TypeSpec.classBuilder(name))

    // Methods
    fun primaryConstructor(build: Constructor.() -> Unit) {
        builder.primaryConstructor(Constructor().apply(build).spec)
    }

    fun constructor(build: Constructor.() -> Unit) {
        builder.addFunction(Constructor().apply(build).spec)
    }

    fun companion(name: String? = null, build: Companion.() -> Unit) {
        builder.addType(Companion(name).apply(build).spec)
    }

    // Classe
    class Companion(name: String? = null): Object(TypeSpec.companionObjectBuilder(name))
}