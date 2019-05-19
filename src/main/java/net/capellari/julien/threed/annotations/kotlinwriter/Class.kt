package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

class Class: Type {
    // Constructors
    constructor(name: ClassName): super(TypeSpec.classBuilder(name))
    constructor(name: String): super(TypeSpec.classBuilder(name))

    // Methods
    fun primaryConstructor(build: Constructor.() -> Unit) {
        builder.primaryConstructor(Constructor().apply(build).spec)
    }

    fun addConstructor(build: Constructor.() -> Unit) {
        builder.addFunction(Constructor().apply(build).spec)
    }
}