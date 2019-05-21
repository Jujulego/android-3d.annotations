package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.CodeBlock
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Codable

@KotlinMarker
class Code:
        AbsWrapper<CodeBlock,CodeBlock.Builder>(CodeBlock.builder()),
        Codable<CodeBlock,CodeBlock.Builder,Code> {

    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    // - comments
    override fun comment(format: String, vararg args: Any) {
        builder.add("// $format", *args)
    }

    // - code
    override fun format(format: String, vararg args: Any) {
        builder.addStatement(format, *args)
    }

    override fun beginFlow(format: String, vararg args: Any) {
        builder.beginControlFlow(format, *args)
    }

    override fun nextFlow(format: String, vararg args: Any) {
        builder.nextControlFlow(format, *args)
    }

    override fun endFlow() {
        builder.endControlFlow()
    }

    fun indent(build: Code.() -> Unit) {
        builder.indent()
        apply(build)
        builder.unindent()
    }
}