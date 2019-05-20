package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.CodeBlock
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Statement

@KotlinMarker
class Code:
        AbsWrapper<CodeBlock,CodeBlock.Builder>(CodeBlock.builder()),
        Statement<CodeBlock,CodeBlock.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    // - code
    override fun format(format: String, vararg args: Any) {
        builder.addStatement(format, *args)
    }

    fun indent(build: Code.() -> Unit) {
        builder.indent()
        apply(build)
        builder.unindent()
    }

    fun flow(format: String, vararg args: Any, build: Code.() -> Unit): ControlFlow
            = ControlFlow(this, format, args, build)

    // Classe
    class ControlFlow(val code: Code, val format: String, val args: Array<out Any?>, val build: Code.() -> Unit, val previous: ControlFlow? = null) {
        // Opérateurs
        operator fun invoke(format: String, vararg args: Any, build: Code.() -> Unit)
                = next(format, *args, build = build)

        // Méthodes
        private fun build(last: Boolean) {
            if (previous == null) {
                code.builder.beginControlFlow(format, args)
            } else {
                previous.build(false)
                code.builder.nextControlFlow(format, args)
            }

            code.apply(build)

            if (last) {
                code.builder.endControlFlow()
            }
        }

        fun next(format: String, vararg args: Any, build: Code.() -> Unit)
                = ControlFlow(code, format, args, build, this)

        fun end() = build(true)
    }
}