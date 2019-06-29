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
    inline fun <reified T> parameters(gen: Generator, build: (Int, Int) -> T): List<T> {
        val params = mutableListOf<T>()
        for (l in 0 until gen.deg) {
            for (c in 0 until gen.deg) {
                params.add(build(l, c))
            }
        }

        return params
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

        val matp = parameters(gen) { l, c -> (names[l] + names[c]) of gen.kcls }.toTypedArray()
        val intf = getInterface(gen, "Matrix")

        val Vec   = ClassName(pkg, getName(gen, "Vec"))

        // Generate class
        val code = createFile(pkg, getName(gen, "Mat")) {
            // Class
            val Mat = class_ { self ->
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

                    function("identity", returns = self) {
                        + "return $self(${parameters(gen) { l, c -> if (l == c) gen.one else gen.zero }.joinToString(", ")})"
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
                constructor("gen" of lambda("l" of Int::class, "c" of Int::class, returns = number)) { (g) ->
                    this_("{ i -> $g(i / ${gen.deg}, i %% ${gen.deg}) }")
                }
                constructor(*matp) {
                    this_("${gen.array_gen}(${matp.joinToString(", ")})")
                }

                // Native methods
                val getDataA = function("getDataA", returns = numberArray) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val getFactor = function("getFactor", "l" of Int::class, "c" of Int::class, returns = number) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val setFactor = function("setFactor", "l" of Int::class, "c" of Int::class, "v" of number) {
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

                val timesM = function("timesM", "mat" of self, returns = Long::class) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val timesMA = function("timesMA", "mat" of self) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                val timesV = function("timesV", "v" of Vec, returns = Long::class) {
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

                override(Any::toString) {
                    + "return \"Mat(\${$dataP.joinToString(\", \")})\""
                }

                val lig = function("lig", "l" of Int::class) { (l) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$l,$it]" }})"
                }

                val col = function("col", "c" of Int::class) { (c) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it,$c]" }})"
                }

                val dataM = template(unbounded("T")) { (T) ->
                    T.reified = true

                    function("data", "f" of lambda("" of numberArray, returns = T), returns = T) { (f) ->
                        modifier(KModifier.INLINE)

                        + "return $dataP.let($f)"
                    }
                }

                if (gen.deg == 4) {
                    function("scale", "fx" of number, "fy" of number, "fz" of number, returns = self) {
                        modifier(KModifier.EXTERNAL)
                    }

                    function("translate", "dx" of number, "dy" of number, "dz" of number, returns = self) {
                        modifier(KModifier.EXTERNAL)
                    }
                }

                // Operator
                operator("get", "l" of Int::class, "c" of Int::class, returns = number) { (l, c) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $getFactor($l, $c)"
                }
                operator("set", "l" of Int::class, "c" of Int::class, "v" of number) { (l, c, v) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $setFactor($l, $c, $v)"
                }

                operator("unaryPlus", returns = self) {
                    modifier(KModifier.OVERRIDE)

                    + "return $self($dataP)"
                }
                operator("unaryMinus", returns = self) {
                    modifier(KModifier.OVERRIDE)

                    + "return $dataM { $self { i -> -it[i] } }"
                }

                operator("plusAssign", "mat" of intf) { (mat) ->
                    modifier(KModifier.OVERRIDE)

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
                    modifier(KModifier.OVERRIDE)

                    + "return $self(this).also { it += $mat }"
                }

                operator("minusAssign", "mat" of intf) { (mat) ->
                    modifier(KModifier.OVERRIDE)

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
                    modifier(KModifier.OVERRIDE)

                    + "return $self(this).also { it -= $mat }"
                }

                operator("timesAssign", "k" of number) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $timesA($k)"
                }
                operator("times", "k" of number) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $dataM { $self { i -> it[i] * $k }}"
                }

                operator("divAssign", "k" of number) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $divA($k)"
                }
                operator("div", "k" of number) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $dataM { $self { i -> it[i] / $k }}"
                }

                /*operator("timesAssign", "mat" of intf) { (mat) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($mat is $self)") {
                        + "$timesMA($mat)"
                    }

                    + "val tmp = $self(this)"
                    flow("for (l in 0 until $size.lig)") {
                        flow("for (c in 0 until $size.col)") {
                            + "this[c,l] = tmp.$lig(l) * $mat.$col(c)"
                        }
                    }
                }*/
                operator("times", "mat" of intf, returns = self) { (mat) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($mat is $self)") {
                        + "return $self($timesM($mat))"
                    }

                    + "return $self { l, c -> $lig(l) * $mat.$col(c) }"
                }

                operator("times", "v" of getInterface(gen, "Vector"), returns = Vec) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($v is ${Vec.simpleName})") {
                        + "return ${Vec.simpleName}($timesV($v))"
                    }

                    + "return ${Vec.simpleName} { $lig(it) * $v }"
                }
            }

            // Utils
            function("matrix", *matp) {
                + "return $Mat(${matp.joinToString(", ")})"
            }

            if (gen.deg == 4) {
                function("scale", "fx" of number, "fy" of number, "fz" of number) { (fx, fy, fz) ->
                    + "return $Mat.identity().scale($fx, $fy, $fz)"
                }

                function("translate", "dx" of number, "dy" of number, "dz" of number) { (dx, dy, dz) ->
                    + "return $Mat.identity().translate($dx, $dy, $dz)"
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}