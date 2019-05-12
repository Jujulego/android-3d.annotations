package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

@KotlinMarker
class File(pkg: String, name: String) {
    // Attributs
    val builder = FileSpec.builder(pkg, name)

    // Propriétés
    val spec get() = builder.build()

    // Méthodes
    fun addClass(name: ClassName, build: Class.() -> Unit) {
        builder.addType(Class(name).apply(build).spec)
    }

    fun addClass(name: String, build: Class.() -> Unit) {
        builder.addType(Class(name).apply(build).spec)
    }
}