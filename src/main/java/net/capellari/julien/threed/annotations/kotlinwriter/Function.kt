package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

class Function(name: String): _Function(FunSpec.builder(name)) {
    // Méthodes
    fun addParameter(name: String, type: TypeName) {
        builder.addParameter(name, type)
    }
    fun addParameter(name: String, type: KClass<*>) {
        builder.addParameter(name, type)
    }
    fun addParameter(name: String, type: java.lang.reflect.Type) {
        builder.addParameter(name, type)
    }

    fun returns(type: TypeName) {
        builder.returns(type)
    }
    fun returns(type: KClass<*>) {
        builder.returns(type)
    }
    fun returns(type: java.lang.reflect.Type) {
        builder.returns(type)
    }
}