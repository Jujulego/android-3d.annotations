package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsFunction
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters

class Constructor : AbsFunction(FunSpec.constructorBuilder()),
        Parameters {

    // Méthodes
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