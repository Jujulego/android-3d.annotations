package net.capellari.julien.threed.annotations.kotlinwriter.bases

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import net.capellari.julien.threed.annotations.kotlinwriter.Code
import net.capellari.julien.threed.annotations.kotlinwriter.KotlinMarker
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import kotlin.reflect.KClass

@KotlinMarker
abstract class AbsFunction(builder: FunSpec.Builder):
        AbsWrapper<FunSpec,FunSpec.Builder>(builder),
        Annotable<FunSpec,FunSpec.Builder>,
        Modifiable<FunSpec, FunSpec.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    override fun annotation(type: ClassName) {
        builder.addAnnotation(type)
    }
    override fun annotation(type: KClass<*>) {
        builder.addAnnotation(type)
    }

    override fun modifiers(vararg modifiers: KModifier) {
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