package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.google.auto.service.AutoService
import net.capellari.julien.threed.annotations.Program
import net.capellari.julien.threed.annotations.math.Generate
import net.capellari.julien.threed.annotations.math.Generators
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@RequiresApi(26)
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(Utils.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ThreedAnnotationProcessor : AbstractProcessor() {
    // Constantes
    private val generators: Map<Generators,AbsGenerator> by lazy {
        mapOf(
            Generators.POINT  to PointGenerator(processingEnv),
            Generators.VECTOR to VectorGenerator(processingEnv),
            Generators.MATRIX to MatrixGenerator(processingEnv)
        )
    }

    private val utils by lazy { Utils(processingEnv) }

    // Méthodes
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            canonicalName<Program>(),
            canonicalName<Generate>()
        )
    }

    override fun process(annotated: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
        env.getElementsAnnotatedWith<Generate>()
            .forEach(this::invokeGenerators)

        return false
    }

    fun invokeGenerators(obj: Element) {
        val annotation = obj.getAnnotation<Generate>()

        annotation.generators.forEach {
            val generator = generators[it.name]

            if (generator != null) {
                generator.generate(obj as TypeElement, it)
            } else {
                utils.log.w("Unknown generator : ${it.name}")
            }
        }
    }
}