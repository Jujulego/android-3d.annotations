package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.threed.annotations.kotlinwriter.*
import net.capellari.julien.threed.annotations.kotlinwriter.Function
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Templatable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.extensionReceiverParameter
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
        builder.addSuperinterface(type, code)
    }
    fun superinterface(type: KClass<*>, delegate: String, vararg args: Any) {
        val code = CodeBlock.builder().add(delegate, *args).build()
        builder.addSuperinterface(type, code)
    }

    // - fonctions
    fun function(name: String, build: Function.() -> Unit)
            = Function(name).apply(build).spec.also { builder.addFunction(it) }

    inline fun function(name: String, vararg params: Parameter, returns: TypeName? = null, receiver: TypeName? = null, crossinline build: Function.() -> Unit)
            = function(name) {
                receiver?.let { receiver(receiver) }
                params.forEach { builder.addParameter(it.spec) }
                returns?.let { returns(returns) }

                this.build()
            }
    inline fun function(name: String, vararg params: Parameter, returns: KClass<*>, receiver: TypeName? = null, crossinline build: Function.() -> Unit)
            = function(name, *params, returns = returns.asTypeName(), receiver = receiver, build = build)
    inline fun function(name: String, vararg params: Parameter, returns: TypeName? = null, receiver: KClass<*>, crossinline build: Function.() -> Unit)
            = function(name, *params, returns = returns, receiver = receiver.asTypeName(), build = build)
    inline fun function(name: String, vararg params: Parameter, returns: KClass<*>, receiver: KClass<*>, crossinline build: Function.() -> Unit)
            = function(name, *params, returns = returns.asTypeName(), receiver = receiver.asTypeName(), build = build)

    inline fun operator(op: Operators, vararg params: Parameter, returns: TypeName? = null, receiver: TypeName? = null, crossinline build: Function.() -> Unit)
            = function(op.fname, *params, returns = returns, receiver = receiver) {
                modifiers(KModifier.OPERATOR, KModifier.OVERRIDE)

                this.build()
            }
    inline fun operator(op: Operators, vararg params: Parameter, returns: KClass<*>, receiver: TypeName? = null, crossinline build: Function.() -> Unit)
            = operator(op, *params, returns = returns.asTypeName(), receiver = receiver, build = build)
    inline fun operator(op: Operators, vararg params: Parameter, returns: TypeName? = null, receiver: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(op, *params, returns = returns, receiver = receiver.asTypeName(), build = build)
    inline fun operator(op: Operators, vararg params: Parameter, returns: KClass<*>? = null, receiver: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(op, *params, returns = returns?.asTypeName(), receiver = receiver.asTypeName(), build = build)

    inline fun unaryPlus(returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.UNARY_PLUS, returns = returns, build = build)

    inline fun unaryMinus(returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.UNARY_MINUS, returns = returns, build = build)

    inline fun not(returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.NOT, returns = returns, build = build)

    inline fun inc(returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.INC, returns = returns, build = build)

    inline fun dec(returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.DEC, returns = returns, build = build)

    inline fun plus(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.PLUS, param, returns = returns, build = build)
    inline fun plus(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.PLUS, param, returns = returns, build = build)

    inline fun minus(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.MINUS, param, returns = returns, build = build)
    inline fun minus(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.MINUS, param, returns = returns, build = build)

    inline fun times(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.TIMES, param, returns = returns, build = build)
    inline fun times(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.TIMES, param, returns = returns, build = build)

    inline fun div(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.DIV, param, returns = returns, build = build)
    inline fun div(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.DIV, param, returns = returns, build = build)

    inline fun rem(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.REM, param, returns = returns, build = build)
    inline fun rem(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.REM, param, returns = returns, build = build)

    inline fun rangeTo(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.RANGE_TO, param, returns = returns, build = build)
    inline fun rangeTo(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.RANGE_TO, param, returns = returns, build = build)

    inline fun contains(param: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.CONTAINS, param, returns = returns, build = build)
    inline fun contains(param: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.CONTAINS, param, returns = returns, build = build)

    inline fun get(vararg params: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.GET, *params, returns = returns, build = build)
    inline fun get(vararg params: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.GET, *params, returns = returns, build = build)

    inline fun set(vararg params: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.SET, *params, build = build)

    inline fun invoke(vararg params: Parameter, returns: TypeName, crossinline build: Function.() -> Unit)
            = operator(Operators.INVOKE, *params, returns = returns, build = build)
    inline fun invoke(vararg params: Parameter, returns: KClass<*>, crossinline build: Function.() -> Unit)
            = operator(Operators.INVOKE, *params, returns = returns, build = build)

    inline fun plusAssign(param: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.PLUS_ASSIGN, param, build = build)

    inline fun minusAssign(param: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.MINUS_ASSIGN, param, build = build)

    inline fun timesAssign(param: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.TIMES_ASSIGN, param, build = build)

    inline fun divAssign(param: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.DIV_ASSIGN, param, build = build)

    inline fun remAssign(param: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.REM_ASSIGN, param, build = build)

    inline fun compareTo(param: Parameter, crossinline build: Function.() -> Unit)
            = operator(Operators.COMPARE_TO, param, returns = Int::class, build = build)

    inline fun <R> override(func: KFunction<R>, crossinline build: Function.() -> Unit)
            = function(func.name) {
                modifiers(KModifier.OVERRIDE)
                if (func.isInfix) modifiers(KModifier.INFIX)

                val extParam = func.extensionReceiverParameter

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