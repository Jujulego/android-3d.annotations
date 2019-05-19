package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ParameterSpec
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import kotlin.reflect.KClass

@KotlinMarker
class Parameter:
        AbsWrapper<ParameterSpec,ParameterSpec.Builder>,
        Annotable<ParameterSpec,ParameterSpec.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Constructeur
    constructor(name: String, type: TypeName): super(ParameterSpec.builder(name, type))
    constructor(name: String, type: KClass<*>): super(ParameterSpec.builder(name, type))

    // Méthodes
    override fun annotation(type: ClassName) {
        builder.addAnnotation(type)
    }
    override fun annotation(type: KClass<*>) {
        builder.addAnnotation(type)
    }
}