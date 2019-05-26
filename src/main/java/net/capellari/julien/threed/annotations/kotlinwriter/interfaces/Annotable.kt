package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

interface Annotable {
    // Méthodes
    fun annotation(type: ClassName)
    fun annotation(type: KClass<*>)
}