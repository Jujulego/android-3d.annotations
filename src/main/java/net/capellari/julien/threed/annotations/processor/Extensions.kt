package net.capellari.julien.threed.annotations.processor

import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

// Annotations
inline fun <reified T: Annotation> canonicalName()
        = T::class.java.canonicalName

inline fun <reified T: Annotation> RoundEnvironment.getElementsAnnotatedWith(): Set<Element>
        = getElementsAnnotatedWith(T::class.java)

inline fun <reified T: Annotation> Element.getAnnotation(): T
        = getAnnotation(T::class.java)

// TypeMirror name
val TypeMirror.canonicalName: String get() = toString()
val TypeMirror.simpleName: String get() {
    val types = canonicalName.split(Regex("[<,>]"))

    // Cas simple
    if (types.size == 1) {
        return types.first().split('.').last()
    }

    // Cas des templates
    var result = ""

    for (i in 0 until types.size-1) {
        result += types[i].split('.').last()
        result += when(i) { 0 -> "<"; types.size-2 -> ">"; else -> "," }
    }

    return result
}