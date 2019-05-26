package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.*
import net.capellari.julien.threed.annotations.kotlinwriter.*
import net.capellari.julien.threed.annotations.kotlinwriter.Function
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Templatable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

@KotlinMarker
abstract class AbsType(builder: TypeSpec.Builder): AbsWrapper<TypeSpec,TypeSpec.Builder>(builder),
        Annotable, Modifiable, Templatable {

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

    override fun typeParameter(name: String, build: TypeParameter.() -> Unit)
            = TypeParameter(name).apply(build).spec.also { builder.addTypeVariable(it) }

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

    fun superinterface(type: TypeName, delegate: String, vararg args: Any) {
        val code = CodeBlock.builder().add(delegate, *args).build()
        builder.addSuperinterface(type, delegate)
    }
    fun superinterface(type: KClass<*>, delegate: String, vararg args: Any) {
        val code = CodeBlock.builder().add(delegate, *args).build()
        builder.addSuperinterface(type, delegate)
    }

    // - fonctions
    fun function(name: String, build: Function.() -> Unit)
            = Function(name).apply(build).spec.also { builder.addFunction(it) }

    fun <R> override(func: KFunction<R>, build: Function.() -> Unit)
            = function(func.name) {
                modifiers(KModifier.OVERRIDE)
                if (func.isInfix) modifiers(KModifier.INFIX)

                val extParam = func.extensionReceiverParameter
                val thisParam = func.instanceParameter

                if (extParam != null) {
                    receiver(extParam.type.asTypeName())
                }

                func.valueParameters.forEach {
                    val name = it.name
                    val type = it.type.asTypeName()

                    if (name == null) {
                        parameter(type)
                    } else {
                        parameter(name, type) {
                            if (it.isVararg) modifiers(KModifier.VARARG)
                        }
                    }
                }

                returns(func.returnType.asTypeName())

                this.build()
            }

    // - propriétés
    fun property(name: String, type: TypeName, build: Property.() -> Unit = {})
            = Property(name, type).apply(build).spec.also { builder.addProperty(it) }

    fun property(name: String, type: KClass<*>, build: Property.() -> Unit = {})
            = Property(name, type).apply(build).spec.also { builder.addProperty(it) }
}