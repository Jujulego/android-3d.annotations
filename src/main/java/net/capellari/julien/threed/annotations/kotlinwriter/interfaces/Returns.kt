package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

interface Returns : Wrapper<FunSpec,FunSpec.Builder> {
    // Méthodes
    fun returns(type: TypeName) {
        builder.returns(type)
    }
    fun returns(type: KClass<*>) {
        builder.returns(type)
    }
}