package net.capellari.julien.threed.annotations.processor

import com.squareup.kotlinpoet.ClassName
import net.capellari.julien.threed.annotations.math.Generator
import net.capellari.julien.threed.annotations.math.NumberType
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

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

// AbsGenerator
val Generator.identifier: String get() = when(type) {
    NumberType.INT   -> "i"
    NumberType.FLOAT -> "f"
}

val Generator.zero: String get() = when(type) {
    NumberType.INT   -> "0"
    NumberType.FLOAT -> "0f"
}

val Generator.degree_cls get() = ClassName("net.capellari.julien.threed.math", "D$deg")

val Generator.kcls: KClass<*> get() = when(type) {
    NumberType.INT   -> Int::class
    NumberType.FLOAT -> Float::class
}

val Generator.karray: KClass<*> get() = when(type) {
    NumberType.INT   -> IntArray::class
    NumberType.FLOAT -> FloatArray::class
}

val Generator.array_name: String get() = when(type) {
    NumberType.INT   -> "IntArray"
    NumberType.FLOAT -> "FloatArray"
}

val Generator.array_gen: String get() = when(type) {
    NumberType.INT   -> "intArrayOf"
    NumberType.FLOAT -> "floatArrayOf"
}