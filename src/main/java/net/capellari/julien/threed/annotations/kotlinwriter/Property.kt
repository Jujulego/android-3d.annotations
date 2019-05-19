package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.*
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsFunction
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass

@KotlinMarker
class Property:
        AbsWrapper<PropertySpec,PropertySpec.Builder>,
        Annotable<PropertySpec, PropertySpec.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Constructeurs
    constructor(name: String, type: TypeName): super(PropertySpec.builder(name, type))
    constructor(name: String, type: KClass<*>): super(PropertySpec.builder(name, type))

    // Méthodes
    override fun annotation(type: ClassName) {
        builder.addAnnotation(type)
    }
    override fun annotation(type: KClass<*>) {
        builder.addAnnotation(type)
    }

    fun addModifiers(vararg modifiers: KModifier) {
        builder.addModifiers(*modifiers)
    }

    fun getter(build: Getter.() -> Unit) {
        builder.getter(Getter().apply(build).spec)
    }

    fun setter(build: Setter.() -> Unit) {
        builder.getter(Setter().apply(build).spec)
    }

    // Classes
    class Getter : AbsFunction(FunSpec.getterBuilder())
    class Setter : AbsFunction(FunSpec.setterBuilder()), Parameters
}