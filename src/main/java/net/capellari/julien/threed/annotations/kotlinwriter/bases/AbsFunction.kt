package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import net.capellari.julien.threed.annotations.kotlinwriter.Code
import net.capellari.julien.threed.annotations.kotlinwriter.Function
import net.capellari.julien.threed.annotations.kotlinwriter.KotlinMarker
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Statement
import kotlin.reflect.KClass

@KotlinMarker
abstract class AbsFunction(builder: FunSpec.Builder):
        AbsWrapper<FunSpec, FunSpec.Builder>(builder),
        Statement<FunSpec, FunSpec.Builder>,
        Annotable<FunSpec, FunSpec.Builder>,
        Modifiable<FunSpec, FunSpec.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    // - annotations
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

    // - code
    fun code(build: Code.() -> Unit) {
        builder.addCode(Code().apply(build).spec)
    }

    override fun format(format: String, vararg args: Any) {
        builder.addStatement(format, *args)
    }

    fun flow(format: String, vararg args: Any, build: AbsFunction.() -> Unit): ControlFlow
            = ControlFlow(this, format, args, build)

    // Classe
    class ControlFlow(val func: AbsFunction, val format: String, val args: Array<out Any?>, val build: AbsFunction.() -> Unit, val previous: ControlFlow? = null) {
        // Opérateurs
        operator fun invoke(format: String, vararg args: Any, build: AbsFunction.() -> Unit)
                = next(format, *args, build = build)

        // Méthodes
        private fun build(last: Boolean) {
            if (previous == null) {
                func.builder.beginControlFlow(format, args)
            } else {
                previous.build(false)
                func.builder.nextControlFlow(format, args)
            }

            func.apply(build)

            if (last) {
                func.builder.endControlFlow()
            }
        }

        fun next(format: String, vararg args: Any, build: AbsFunction.() -> Unit)
                = ControlFlow(func, format, args, build, this)

        fun end() = build(true)
    }
}