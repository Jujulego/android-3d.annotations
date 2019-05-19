package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

interface Parameters : Wrapper<FunSpec,FunSpec.Builder> {
    // Méthodes
    fun addParameter(name: String, type: TypeName) {
        builder.addParameter(name, type)
    }
    fun addParameter(name: String, type: KClass<*>) {
        builder.addParameter(name, type)
    }
}