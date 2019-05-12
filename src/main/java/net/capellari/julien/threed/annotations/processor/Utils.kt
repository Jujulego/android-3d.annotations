package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

internal class Utils(val processingEnv: ProcessingEnvironment) {
    // Companion
    companion object {
        // Constantes
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        // Classes
        class Logger(val processingEnv: ProcessingEnvironment) {
            // Levels
            fun o(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.OTHER, msg)
            fun n(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, msg)
            fun w(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, msg)
            fun mw(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg)
            fun e(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)
        }
    }

    // Attributs
    val log = Logger(processingEnv)

    @RequiresApi(26)
    val sourceDir = Paths.get(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME])

    // Elements
    fun type(name: String): TypeElement = processingEnv.elementUtils.getTypeElement(name)

    fun packageOf(el: Element): PackageElement = processingEnv.elementUtils.getPackageOf(el)

    // Types
    fun inherit(type: TypeElement, base: TypeMirror)
            = processingEnv.typeUtils.isSameType(type.superclass, base)
    fun inherit(type: TypeElement, base: String)
            = inherit(type, type(base).asType())

    fun implements(type: TypeElement, base: TypeMirror)
            = type.interfaces.any { processingEnv.typeUtils.isSameType(it, base) }
    fun implements(type: TypeElement, base: String)
            = implements(type, type(base).asType())

    // Poet
    fun code(gen: CodeBlock.Builder.() -> Unit): CodeBlock {
        return CodeBlock.builder().apply(gen).build()
    }

    @RequiresApi(26)
    fun writeTo(output: Path, code: FileSpec) {
        // Create directory(ies)
        Files.createDirectories(output)

        // Write to file
        code.writeTo(output)
    }
}