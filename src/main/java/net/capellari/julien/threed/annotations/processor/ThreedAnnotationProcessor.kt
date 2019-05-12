package net.capellari.julien.threed.annotations.processor

import com.google.auto.service.AutoService
import net.capellari.julien.threed.annotations.Program
import net.capellari.julien.threed.annotations.math.PointClass
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(Utils.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ThreedAnnotationProcessor : AbstractProcessor() {
    // Méthodes
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            canonicalName<Program>(),

            // math
            canonicalName<PointClass>()
        )
    }

    override fun process(annotated: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
        env.getElementsAnnotatedWith<PointClass>()
            .forEach(PointGenerator(processingEnv)::invoke)

        return false
    }
}