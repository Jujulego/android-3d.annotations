package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.kotlinwriter2.*
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

@RequiresApi(26)
class VectorGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
    // Functions
    private fun getName(gen: Generator, t: String): String {
        return "$t${gen.deg}${gen.identifier}"
    }

    private fun getCoordParameters(gen: Generator)
            = (0 until gen.deg).map { "v$it" of gen.kcls }.toTypedArray()

    private fun genGeneratorType(gen: Generator): LambdaTypeName {
        return LambdaTypeName.Companion.get(
            null,
            Int::class.asTypeName(),
            returnType = gen.kcls.asTypeName()
        )
    }

    private fun getInterface(gen: Generator, name: String): TypeName {
        return ClassName("net.capellari.julien.threed.math", name)
            .parameterizedBy(gen.kcls.asTypeName(), gen.degree_cls)
    }

    override fun generate(base: TypeElement, gen: Generator) {
        // Get infos
        val pkg = "net.capellari.julien.threed"
        val clsName = ClassName(pkg, getName(gen, "Vec"))

        val number = gen.kcls
        val numberArray = gen.karray

        val baseName = base.asClassName()
            .parameterizedBy(number.asTypeName())

        val coords = getCoordParameters(gen)

        // Generate class
        val code = createFile(clsName) {
            // Classes
            class_(clsName) {
                // superclass
                superclass("net.capellari.julien.threed.jni", "JNIClass", "handle")

                // interface
                superinterface(baseName)
                superinterface(getInterface(gen, "Vector"))

                // Companion
                companion {
                    function("create", *coords, returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }

                    function("createA", "factors" of numberArray, returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }

                    function("createC", "v" of clsName, returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }
                }

                // Native methods
                val getDataA = function("getDataA", returns = numberArray) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val getCoord = function("getCoord", "i" of Int::class, returns = number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val setCoord = function("setCoord", "i" of Int::class, "v" of number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val equal = function("equal", "other" of clsName, returns = Boolean::class) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                // Propriétés
                val data = property("data" of numberArray) {
                    getter {
                        + "return $getDataA()"
                    }
                }

                // Constructeurs
                constructor("handle" of Long::class, primary = true) {
                    modifier(KModifier.INTERNAL)
                }

                constructor {
                    this_("create(${(0 until gen.deg).joinToString(", ") { gen.zero }})")
                }

                constructor(*coords) {
                    this_("create(${coords.joinToString(", ")})")
                }

                constructor("factors" of numberArray) { (factors) ->
                    this_("createA($factors)")
                }

                constructor("gen" of genGeneratorType(gen)) { (g) ->
                    this_("${gen.array_name}(${gen.deg}, $g)")
                }

                constructor("v" of clsName) { (v) ->
                    this_("createC($v)")
                }

                // Opérateurs
                get("i" of Int::class, returns = number) { (i) ->
                    + "return $getCoord($i)"
                }

                set("i" of Int::class, "v" of number) { (i, v) ->
                    + "return $setCoord($i, $v)"
                }

                unaryPlus(returns = clsName) {
                    + "return ${clsName.simpleName}(this)"
                }
                unaryMinus(returns = clsName) {
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "-this[$it]" }})"
                }

                plusAssign("v" of getInterface(gen, "Vector")) { (v) ->
                    for (i in 0 until gen.deg) {
                        + "this[$i] += $v[$i]"
                    }
                }
                minusAssign("v" of getInterface(gen, "Vector")) { (v) ->
                    for (i in 0 until gen.deg) {
                        + "this[$i] -= $v[$i]"
                    }
                }

                timesAssign("k" of gen.kcls) { (k) ->
                    for (i in 0 until gen.deg) {
                        + "this[$i] *= $k"
                    }
                }
                divAssign("k" of gen.kcls) { (k) ->
                    for (i in 0 until gen.deg) {
                        + "this[$i] /= $k"
                    }
                }

                plus("v" of getInterface(gen, "Vector"), returns = clsName) { (v) ->
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] + $v[$it]" }})"
                }
                minus("v" of getInterface(gen, "Vector"), returns = clsName) { (v) ->
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] - $v[$it]" }})"
                }

                times("k" of gen.kcls, returns = clsName) { (k) ->
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] * $k" }})"
                }
                div("k" of gen.kcls, returns = clsName) { (k) ->
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] / $k" }})"
                }

                times("c" of getInterface(gen, "Coord"), returns = number) { (c) ->
                    + "return ${(0 until gen.deg).joinToString(" + ") { "(this[$it] * $c[$it])" }}"
                }

                // Méthodes
                override(Any::equals) { (other) ->
                    flow("if ($other === this)") {
                        + "return true"
                    }

                    flow("if ($other is ${clsName.simpleName})") {
                        + "return $equal($other)"
                    }

                    + "return super.equals($other)"
                }

                override(Any::hashCode) {
                    + "return $data.contentHashCode()"
                }

                override(Any::toString) {
                    + "return \"Vec(${(0 until gen.deg).joinToString(", ") { "\${this[$it]}" }})\""
                }
            }

            // Functions
            function("vector", *coords, returns = clsName) {
                + "return $clsName(${(0 until gen.deg).joinToString(", ") { "v$it" }})"
            }

            if (gen.deg == 3) {
                function("cross", "v" of clsName, receiver = clsName, returns = clsName) { (v) ->
                    modifier(KModifier.INFIX)

                    + "return $clsName(this[1] * $v[2] - this[2] * $v[1], this[0] * $v[2] - this[2] * $v[0], this[0] * $v[1] - this[1] * $v[0])"
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}