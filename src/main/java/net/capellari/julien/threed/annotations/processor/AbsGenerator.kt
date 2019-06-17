package net.capellari.julien.threed.annotations.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import net.capellari.julien.kotlinwriter.of
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

abstract class AbsGenerator(processingEnv: ProcessingEnvironment) {
    // Propriétés
    internal val utils = Utils(processingEnv)

    // Méthodes
    abstract fun generate(base: TypeElement, gen: Generator)

    // utils
    protected fun getName(gen: Generator, t: String): String {
        return "$t${gen.deg}${gen.identifier}"
    }

    protected fun getCoordParameters(gen: Generator)
            = (0 until gen.deg).map { "v$it" of gen.kcls }.toTypedArray()

    protected fun genGeneratorType(gen: Generator): LambdaTypeName {
        return LambdaTypeName.Companion.get(
            null,
            Int::class.asTypeName(),
            returnType = gen.kcls.asTypeName()
        )
    }

    protected fun getInterface(gen: Generator, name: String): TypeName {
        return ClassName("net.capellari.julien.threed.math", name)
            .parameterizedBy(gen.kcls.asTypeName(), gen.degree_cls)
    }
}