package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.kotlinwriter.*
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

@RequiresApi(26)
class MatrixGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
    val names = arrayOf("a", "b", "c", "d")

    // Functions
    fun getMatrixParameters(gen: Generator): Array<Parameter> {
        val params = mutableListOf<Parameter>()
        for (i in 0 until gen.deg) {
            for (j in 0 until gen.deg) {
                params.add("${names[i]}${names[j]}" of gen.kcls)
            }
        }

        return params.toTypedArray()
    }

    fun getMatSize(gen: Generator): ParameterizedTypeName {
        return ClassName("net.capellari.julien.threed.math", "MatSize")
            .parameterizedBy(gen.degree_cls, gen.degree_cls)
    }

    override fun generate(base: TypeElement, gen: Generator) {
        // Get infos
        val pkg = "net.capellari.julien.threed"

        val number = gen.kcls
        val numberArray = gen.karray

        val matp = getMatrixParameters(gen)
        val intf = getInterface(gen, "Matrix")

        val Point  = ClassName(pkg, getName(gen, "Point"))
        val Vec = ClassName(pkg, getName(gen, "Vec"))

        // Generate class
        val code = createFile(pkg, getName(gen, "Mat")) {
            // Classe
            class_ { self ->
                // superclass
                superclass("net.capellari.julien.threed.jni", "JNIClass", "handle")
                superinterface(intf)

                // Companion
                companion {
                    function("create", returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }

                    function("createA", "factors" of numberArray, returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }

                    function("createM", "v" of self, returns = Long::class) {
                        annotate<JvmStatic>()
                        modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }
                }

                // Constructors
                constructor("handle" of Long::class, primary = true) {
                    modifier(KModifier.INTERNAL)
                    super_("handle")
                }
                constructor("factors" of numberArray) {
                    modifier(KModifier.INTERNAL)
                    this_("createA(factors)")
                }

                constructor {
                    this_("create()")
                }
                constructor("mat" of self) { (mat) ->
                    this_("createM($mat)")
                }
                constructor("gen" of genGeneratorType(gen)) { (g) ->
                    this_("${gen.array_name}(${gen.deg * gen.deg}, $g)")
                }
                constructor(*matp) {
                    this_("${gen.array_gen}(${matp.joinToString(", ")})")
                }

                // Native methods
                val getDataA = function("getDataA", returns = numberArray) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val getFactor = function("getFactor", "c" of Int::class, "l" of Int::class, returns = number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val setFactor = function("setFactor", "c" of Int::class, "l" of Int::class, "v" of number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val equal = function("equal", "mat" of self, returns = Boolean::class) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val plusA = function("plusA", "mat" of self) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val minusA = function("minusA", "mat" of self) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val timesA = function("timesA", "k" of number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val divA = function("divA", "k" of number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                // Properties
                val size = property("size" of getMatSize(gen) default "MatSize(D${gen.deg}, D${gen.deg})") {
                    modifier(KModifier.OVERRIDE)
                }
                val dataP = property("data" of numberArray) {
                    getter {
                        + "return $getDataA()"
                    }
                }

                // Methods
                override(Any::equals) { (other) ->
                    flow("if ($other === this)") {
                        + "return true"
                    }

                    flow("if ($other is $self)") {
                        + "return $equal($other)"
                    }

                    + "return false"
                }

                override(Any::hashCode) {
                    + "return $dataP.contentHashCode()"
                }

                val lig = function("lig", "l" of Int::class) { (l) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it,$l]" }})"
                }

                function("col", "c" of Int::class) { (c) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$c,$it]" }})"
                }

                val dataM = template(unbounded("T")) { (T) ->
                    T.reified = true

                    function("data", "f" of lambda("" of numberArray, returns = T), returns = T) { (f) ->
                        modifier(KModifier.INLINE)

                        + "return $dataP.let($f)"
                    }
                }

                // Operator
                operator("get", "c" of Int::class, "l" of Int::class, returns = number) { (c, l) ->
                    + "return $getFactor($c, $l)"
                }
                operator("set", "c" of Int::class, "l" of Int::class, "v" of number) { (c, l, v) ->
                    + "return $setFactor($c, $l, $v)"
                }

                operator("unaryPlus", returns = self) {
                    + "return $self($dataP)"
                }
                operator("unaryMinus", returns = self) {
                    + "return $dataM { $self { i -> -it[i] } }"
                }

                operator("plusAssign", "mat" of intf) { (mat) ->
                    flow("if ($mat is $self)") {
                        + "return $plusA($mat)"
                    }

                    flow("for (l in 0 until $size.lig)") {
                        flow("for (c in 0 until $size.col)") {
                            + "this[c,l] += $mat[c,l]"
                        }
                    }
                }
                operator("plus", "mat" of intf) { (mat) ->
                    + "return $self(this).also { it += $mat }"
                }

                operator("minusAssign", "mat" of intf) { (mat) ->
                    flow("if ($mat is $self)") {
                        + "return $minusA($mat)"
                    }

                    flow("for (l in 0 until $size.lig)") {
                        flow("for (c in 0 until $size.col)") {
                            + "this[c,l] -= $mat[c,l]"
                        }
                    }
                }
                operator("minus", "mat" of intf) { (mat) ->
                    + "return $self(this).also { it -= $mat }"
                }

                operator("timesAssign", "k" of number) { (k) ->
                    + "return $timesA($k)"
                }
                operator("times", "k" of number) { (k) ->
                    + "return $dataM { $self { i -> it[i] * $k }}"
                }
                operator("times", "pt" of getInterface(gen, "Point"), returns = Point) { (pt) ->
                    + "return ${Point.simpleName} { $lig(it) * $pt }"
                }
                operator("times", "v" of getInterface(gen, "Vector"), returns = Vec) { (v) ->
                    + "return ${Vec.simpleName} { $lig(it) * $v }"
                }

                operator("divAssign", "k" of number) { (k) ->
                    + "return $divA($k)"
                }
                operator("div", "k" of number) { (k) ->
                    + "return $dataM { $self { i -> it[i] / $k }}"
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}