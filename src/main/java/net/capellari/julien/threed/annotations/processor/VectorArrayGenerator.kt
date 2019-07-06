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
                    function("create", "n" of Int::class, returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }
                }

                // Properties
                property("size" of Int::class) {
                    modifier(KModifier.OVERRIDE)

                    getter {
                        modifier(KModifier.EXTERNAL)
                    }
                }

                // Constructors
                constructor("handle" of Long::class, primary = true) {
                    modifier(KModifier.INTERNAL)
                    super_("handle")
                }

                constructor("n" of Int::class default 0) { (n) ->
                    this_("create($n)")
                }

                // Operators
                operator("get", "i" of Int::class, returns = Vec) {
                    modifier(KModifier.OVERRIDE, KModifier.EXTERNAL)
                }
                operator("set", "i" of Int::class, "value" of Vec) {
                    modifier(KModifier.OVERRIDE, KModifier.EXTERNAL)
                }

                // Methods
                function("add", "element" of Vec, returns = Boolean::class) {
                    modifier(KModifier.OVERRIDE, KModifier.EXTERNAL)
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}