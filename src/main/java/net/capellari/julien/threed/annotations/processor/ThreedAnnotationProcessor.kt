package net.capellari.julien.threed.annotations.processor

import net.capellari.julien.threed.annotations.Program
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions(ThreedAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ThreedAnnotationProcessor : AbstractProcessor() {
    // Companion
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    // Méthodes
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            Program::class.java.canonicalName
        )
    }

    override fun process(annotated: MutableSet<out TypeElement>?, env: RoundEnvironment?): Boolean {
        return false
    }
}