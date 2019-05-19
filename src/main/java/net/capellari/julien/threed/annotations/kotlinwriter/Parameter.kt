package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ParameterSpec
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import kotlin.reflect.KClass

@KotlinMarker
class Parameter : AbsWrapper<ParameterSpec,ParameterSpec.Builder> {
    // Propriétés
    override val spec get() = builder.build()

    // Constructeur
    constructor(name: String, type: TypeName): super(ParameterSpec.builder(name, type))
    constructor(name: String, type: KClass<*>): super(ParameterSpec.builder(name, type))
}