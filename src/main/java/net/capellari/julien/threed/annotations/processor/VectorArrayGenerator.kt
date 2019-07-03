package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.kotlinwriter.*
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

@RequiresApi(26)
class VectorArrayGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
    // Methods
    override fun generate(base: TypeElement, gen: Generator) {
        // Get infos
        val pkg = "net.capellari.julien.threed"

        val number = gen.kcls
        val numberArray = gen.karray

        val Mat = ClassName(pkg, getName(gen, "Mat"))
        val Vec = ClassName(pkg, getName(gen, "Vec"))

        val base_tpl = ClassName("$pkg.math", "VectorArray")
        val base_name = base_tpl.parameterizedBy(Vec)

        // Generate class
        val code = createFile(pkg, getName(gen, "VectorArray")) {
            // Classes
            val VecArr = class_ { self ->
                // superclass
                superclass("net.capellari.julien.threed.jni", "JNIClass", "handle")

                // interfaces
                superinterface(base_name)

                // Companion
                companion {
                    function("create", returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }
                }

                // Properties
                property("size" of Int::class default 0) {
                    modifier(KModifier.OVERRIDE)
                }

                // Constructors
                constructor("handle" of Long::class, primary = true) {
                    modifier(KModifier.INTERNAL)
                    super_("handle")
                }

                constructor {
                    this_("create()")
                }

                // Operators
                operator("get", "i" of Int::class, returns = Vec) { (i) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}()"
                }
                operator("set", "i" of Int::class, "value" of Vec) { (i, v) ->
                    modifier(KModifier.OVERRIDE)
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}