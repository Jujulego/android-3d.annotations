package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.*
import net.capellari.julien.threed.annotations.kotlinwriter.Code
import net.capellari.julien.threed.annotations.kotlinwriter.Function
import net.capellari.julien.threed.annotations.kotlinwriter.KotlinMarker
import net.capellari.julien.threed.annotations.kotlinwriter.Property
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import kotlin.reflect.KClass

@KotlinMarker
abstract class AbsType(builder: TypeSpec.Builder):
        AbsWrapper<TypeSpec,TypeSpec.Builder>(builder),
        Annotable<TypeSpec, TypeSpec.Builder>,
        Modifiable<TypeSpec, TypeSpec.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    // - annotation
    override fun annotation(type: ClassName) {
        builder.addAnnotation(type)
    }
    override fun annotation(type: KClass<*>) {
        builder.addAnnotation(type)
    }

    // - modifiers
    override fun modifiers(vararg modifiers: KModifier) {
        builder.addModifiers(*modifiers)
    }

    // - superclass
    fun superclass(pkg: String, name: String) = superclass(ClassName(pkg, name))
    fun superclass(type: TypeName) { builder.superclass(type) }
    fun superclass(type: KClass<*>) { builder.superclass(type) }

    fun superclassParameter(format: String, vararg args: String) {
        builder.addSuperclassConstructorParameter(format, *args)
    }
    fun superclassParameter(build: Code.() -> Unit) {
        builder.addSuperclassConstructorParameter(Code().apply(build).spec)
    }

    fun superinterface(type: TypeName) { builder.addSuperinterface(type) }
    fun superinterface(type: KClass<*>) { builder.addSuperinterface(type) }
    fun superinterface(pkg: String, name: String) = superinterface(ClassName(pkg, name))

    // - fonctions
    fun function(name: String, build: Function.() -> Unit)
            = Function(name).apply(build).spec.also { builder.addFunction(it) }

    // - propriétés
    fun property(name: String, type: TypeName, build: Property.() -> Unit = {})
            = Property(name, type).apply(build).spec.also { builder.addProperty(it) }

    fun property(name: String, type: KClass<*>, build: Property.() -> Unit = {})
            = Property(name, type).apply(build).spec.also { builder.addProperty(it) }
}