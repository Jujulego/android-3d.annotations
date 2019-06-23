package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.kotlinwriter.*
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

@RequiresApi(26)
class PointGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
    // Functions
    override fun generate(base: TypeElement, gen: Generator) {
        // Get infos
        val pkg = "net.capellari.julien.threed"

        val number = gen.kcls
        val numberArray = gen.karray

        val baseName = base.asClassName()
            .parameterizedBy(number.asTypeName())

        val coords = getCoordParameters(gen)

        val Mat = ClassName(pkg, getName(gen, "Mat"))

        // Generate class
        val code = createFile(pkg, getName(gen, "Point")) {
            // Classe
            val Point = class_ { self ->
                // superclass
                superclass("net.capellari.julien.threed.jni", "JNIClass", "handle")

                // interface
                superinterface(baseName)
                superinterface(getInterface(gen, "Point"))

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

                // Constructors
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

                constructor("pt" of self) { (pt) ->
                    this_("createC($pt)")
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

                val timesMA = function("timesMA", "mat" of Mat) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                // Propriétés
                val data = property("data" of numberArray) {
                    getter {
                        + "return $getDataA()"
                    }
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
                operator("minusAssign", "v" of getInterface(gen, "Vector")) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    for (i in 0 until gen.deg) {
                        + "this[$i] -= $v[$i]"
                    }
                }

                operator("plus", "v" of getInterface(gen, "Vector"), returns = self) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "this[$it] + $v[$it]" }})"
                }
                operator("minus", "v" of getInterface(gen, "Vector"), returns = self) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(${(0 until gen.deg).joinToString(", ") { "this[$it] - $v[$it]" }})"
                }

                operator("minus", "pt" of getInterface(gen, "Point"), returns = ClassName(pkg, getName(gen, "Vec"))) { (pt) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${getName(gen, "Vec")}(${(0 until gen.deg).joinToString(", ") { "this[$it] - $pt[$it]" }})"
                }

                operator("timesAssign", "mat" of getInterface(gen, "Matrix")) { (mat) ->
                    modifier(KModifier.OVERRIDE)

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
                    + "return \"Point(\${$data.joinToString(\", \")})\""
                }
            }

            // Utils
            function("point", *coords, returns = Point) {
                + "return $Point(${(0 until gen.deg).joinToString(", ") { "v$it" }})"
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}