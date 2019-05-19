package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import net.capellari.julien.threed.annotations.kotlinwriter.Code
import net.capellari.julien.threed.annotations.kotlinwriter.KotlinMarker

@KotlinMarker
abstract class AbsFunction(builder: FunSpec.Builder): AbsWrapper<FunSpec,FunSpec.Builder>(builder) {
    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    inline fun<reified A: Annotation> addAnnotation() {
        builder.addAnnotation(A::class)
    }

    fun addAnnotation(annotation: ClassName) {
        builder.addAnnotation(annotation)
    }

    fun addModifiers(vararg modifiers: KModifier) {
        builder.addModifiers(*modifiers)
    }

    fun addStatement(format: String, vararg args: Any) {
        builder.addStatement(format, *args)
    }

    fun addCode(build: Code.() -> Unit) {
        builder.addCode(Code().apply(build).spec)
    }

    operator fun String.unaryPlus() {
        builder.addStatement(this)
    }
}