package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper

@KotlinMarker
class File(pkg: String, name: String): AbsWrapper<FileSpec,FileSpec.Builder>(FileSpec.builder(pkg, name)) {
    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    fun addClass(name: ClassName, build: Class.() -> Unit)
            = Class(name).apply(build).spec.also { builder.addType(it) }

    fun addClass(name: String, build: Class.() -> Unit)
            = Class(name).apply(build).spec.also { builder.addType(it) }
}