package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

class Constructor : _Function(FunSpec.constructorBuilder()) {
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

    fun callThis(vararg code: String) {
        builder.callThisConstructor(*code)
    }

    fun callThis(build: Code.() -> Unit) {
        builder.callThisConstructor(Code().apply(build).spec)
    }

    fun callSuper(vararg code: String) {
        builder.callSuperConstructor(*code)
    }

    fun callSuper(build: Code.() -> Unit) {
        builder.callSuperConstructor(Code().apply(build).spec)
    }
}