package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import net.capellari.julien.threed.annotations.kotlinwriter.Parameter
import kotlin.reflect.KClass

interface Parameters : Wrapper<FunSpec,FunSpec.Builder> {
    // Méthodes
    fun parameter(name: String, type: TypeName, build: Parameter.() -> Unit = {}) {
        builder.addParameter(Parameter(name, type).apply(build).spec)
    }
    fun parameter(name: String, type: KClass<*>, build: Parameter.() -> Unit = {}) {
        builder.addParameter(Parameter(name, type).apply(build).spec)
    }

    fun parameters(parameters: Iterable<Parameter>) {
        builder.addParameters(parameters.map { it.spec })
    }
}