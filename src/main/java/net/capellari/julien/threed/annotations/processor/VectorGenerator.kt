package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.kotlinwriter.*
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

@RequiresApi(26)
class VectorGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
    val names = arrayOf("x", "y", "z", "a")

    // Functions
    inline fun <reified T> parameters(deg: Int, build: (Int) -> T): List<T> {
        val params = mutableListOf<T>()
        for (i in 0 until deg) {
            params.add(build(i))
        }

        return params
    }

    override fun generate(base: TypeElement, gen: Generator) {
        // Get infos
        val pkg = "net.capellari.julien.threed"

        val number = gen.kcls
        val numberArray = gen.karray

        val baseName = base.asClassName()
            .parameterizedBy(number.asTypeName())

        val coords = parameters(gen.deg) { i -> names[i] of number }.toTypedArray()

        val Mat = ClassName(pkg, getName(gen, "Mat"))

        // Generate class
        val code = createFile(pkg, getName(gen, "Vec")) {
            // Classes
            val Vec = class_ { self ->
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

                    function("createC", "v" of self, returns = Long::class) {
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

                val equal = function("equal", "other" of self, returns = Boolean::class) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val timesV = function("timesV", "v" of self, returns = number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val timesMA = function("timesMA", "mat" of Mat) {
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
                    super_("handle")
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

                constructor("v" of self) { (v) ->
                    this_("createC($v)")
                }

                // Opérateurs
                operator("get", "i" of Int::class, returns = number) { (i) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $getCoord($i)"
                }
                operator("set", "i" of Int::class, "v" of number) { (i, v) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $setCoord($i, $v)"
                }

                operator("unaryPlus", returns = self) {
                    modifier(KModifier.OVERRIDE)

                    + "return $self(this)"
                }
                operator("unaryMinus", returns = self) {
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "-this[$it]" }})"
                }

                operator("plusAssign", "v" of getInterface(gen, "Vector")) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    for (i in 0 until gen.deg) {
                        + "this[$i] += $v[$i]"
                    }
                }
                operator("plus", "v" of getInterface(gen, "Vector"), returns = self) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "this[$it] + $v[$it]" }})"
                }

                operator("minusAssign", "v" of getInterface(gen, "Vector")) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    for (i in 0 until gen.deg) {
                        + "this[$i] -= $v[$i]"
                    }
                }
                operator("minus", "v" of getInterface(gen, "Vector"), returns = self) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "this[$it] - $v[$it]" }})"
                }

                operator("timesAssign", "k" of gen.kcls) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    for (i in 0 until gen.deg) {
                        + "this[$i] *= $k"
                    }
                }
                operator("times", "k" of gen.kcls, returns = self) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "this[$it] * $k" }})"
                }

                operator("divAssign", "k" of gen.kcls) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    for (i in 0 until gen.deg) {
                        + "this[$i] /= $k"
                    }
                }
                operator("div", "k" of gen.kcls, returns = self) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "this[$it] / $k" }})"
                }

                operator("timesAssign", "mat" of getInterface(gen, "Matrix")) { (mat) ->
                    flow("if ($mat is $Mat)") {
                        + "$timesMA($mat)"
                    }.next("else") {
                        + "val tmp = $self(this)"

                        flow("for (c in 0 until ${gen.deg})") {
                            + "this[c] = tmp * $mat.col(c)"
                        }
                    }
                }
                operator("times", "mat" of getInterface(gen, "Matrix"), returns = self) { (mat) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($mat is $Mat)") {
                        + "return $self(this).also { it *= $mat }"
                    }.next("else") {
                        + "return $self { this * $mat.col(it) }"
                    }
                }

                operator("times", "c" of getInterface(gen, "Coord"), returns = number) { (c) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($c is $self)") {
                        + "return $timesV($c)"
                    }

                    + "return ${(0 until gen.deg).joinToString(" + ") { "(this[$it] * $c[$it])" }}"
                }

                // Méthodes
                override(Any::equals) { (other) ->
                    flow("if ($other === this)") {
                        + "return true"
                    }

                    flow("if ($other is $self)") {
                        + "return $equal($other)"
                    }

                    + "return super.equals($other)"
                }

                override(Any::hashCode) {
                    + "return $data.contentHashCode()"
                }

                override(Any::toString) {
                    + "return \"Vec(\${$data.joinToString(\", \")})\""
                }

                // 3d bonus !
                if (gen.deg == 3) {
                    val ncross = function("ncross", "v" of self, returns = Long::class) {
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }

                    function("cross", "v" of self, returns = self) { (v) ->
                        modifier(KModifier.INFIX)

                        + "return $self($ncross($v))"
                    }
                }
            }

            // Functions
            function("vector", *coords, returns = Vec) {
                + "return $Vec(${coords.joinToString(", ")})"
            }

            if (gen.deg > 2) {
                function("point", *(coords.sliceArray(0 until (gen.deg - 1))), returns = Vec) { coords ->
                    + "return $Vec(${coords.joinToString(", ")}, ${gen.one})"
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}