package net.capellari.julien.threed.annotations.processor

import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

abstract class AbsGenerator(processingEnv: ProcessingEnvironment) {
    // Propriétés
    internal val utils = Utils(processingEnv)

    // Méthodes
    abstract fun generate(base: TypeElement, gen: Generator)
}