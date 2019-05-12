package net.capellari.julien.threed.annotations.processor

import com.squareup.kotlinpoet.*
import java.lang.reflect.Type
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

// Kotlin Poet
// - code blocks
fun FunSpec.Builder.addCode(build: CodeBlock.Builder.() -> Unit) {
    addCode(CodeBlock.builder().apply(build).build())
}

fun FunSpec.Builder.callThisConstructor(build: CodeBlock.Builder.() -> Unit) {
    callThisConstructor(CodeBlock.builder().apply(build).build())
}

fun FunSpec.Builder.callSuperConstructor(build: CodeBlock.Builder.() -> Unit) {
    callSuperConstructor(CodeBlock.builder().apply(build).build())
}

// - functions
fun TypeSpec.Builder.addFunction(name: String, build: FunSpec.Builder.() -> Unit) {
    addFunction(FunSpec.builder(name).apply(build).build())
}

fun TypeSpec.Builder.addConstructor(build: FunSpec.Builder.() -> Unit) {
    addFunction(FunSpec.constructorBuilder().apply(build).build())
}

fun TypeSpec.Builder.addPrimaryConstructor(build: FunSpec.Builder.() -> Unit) {
    primaryConstructor(FunSpec.constructorBuilder().apply(build).build())
}

fun PropertySpec.Builder.getter(build: FunSpec.Builder.() -> Unit) {
    getter(FunSpec.getterBuilder().apply(build).build())
}

fun PropertySpec.Builder.setter(build: FunSpec.Builder.() -> Unit) {
    setter(FunSpec.setterBuilder().apply(build).build())
}

// - property
fun TypeSpec.Builder.addProperty(name: String, type: TypeName, build: PropertySpec.Builder.() -> Unit) {
    addProperty(PropertySpec.builder(name, type).apply(build).build())
}

fun TypeSpec.Builder.addProperty(name: String, type: Type, build: PropertySpec.Builder.() -> Unit) {
    addProperty(PropertySpec.builder(name, type).apply(build).build())
}

fun TypeSpec.Builder.addProperty(name: String, type: KClass<*>, build: PropertySpec.Builder.() -> Unit) {
    addProperty(PropertySpec.builder(name, type).apply(build).build())
}

// - type
fun TypeSpec.Builder.addCompanion(name: String? = null, build: TypeSpec.Builder.() -> Unit) {
    addType(TypeSpec.companionObjectBuilder(name).apply(build).build())
}

fun FileSpec.Builder.addClass(name: ClassName, build: TypeSpec.Builder.() -> Unit) {
    addType(TypeSpec.classBuilder(name).apply(build).build())
}

fun FileSpec.Builder.addClass(name: String, build: TypeSpec.Builder.() -> Unit) {
    addType(TypeSpec.classBuilder(name).apply(build).build())
}