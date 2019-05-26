package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsFunction
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class Function(name: String): AbsFunction(FunSpec.builder(name)),
        Parameters, Returns {

    // Méthodes
    // - receiver
    fun receiver(type: TypeName) {
        builder.receiver(type)
    }
    fun receiver(type: KClass<*>) {
        builder.receiver(type)
    }
}