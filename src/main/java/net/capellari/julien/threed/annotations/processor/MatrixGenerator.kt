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
        val Vec3 = ClassName(pkg, "Vec3${gen.identifier}")

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

                    if (gen.deg == 4) {
                        // rotate
                        val nrotate = function("nrotate", "a" of Double::class, "axe" of Vec3, returns = Long::class) {
                            annotate<JvmStatic>()
                            modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                        }
                        val rotate = function("rotate", "a" of Double::class, "axe" of Vec3, returns = self) { (a, axe) ->
                            + "return $self($nrotate($a, $axe))"
                        }

                        function(rotate.name, "a" of Double::class, "x" of number, "y" of number, "z" of number, returns = self) { (a, x, y, z) ->
                            + "return $rotate($a, $Vec3($x, $y, $z))"
                        }

                        // lookAt
                        val nlookAt = function("nlookAt", "eye" of Vec3, "center" of Vec3, "up" of Vec3, returns = Long::class) {
                            annotate<JvmStatic>()
                            modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                        }
                        function("lookAt", "eye" of Vec3, "center" of Vec3, "up" of Vec3, returns = self) { (eye, center, up) ->
                            + "return $self($nlookAt($eye, $center, $up))"
                        }
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

                val timesM = function("timesM", "mat" of self, returns = self) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
                val timesV = function("timesV", "v" of Vec, returns = Vec) {
                    modifier(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                // Properties
                val size = property("size" of getMatSize(gen) default "MatSize(D${gen.deg}, D${gen.deg})") {
                    modifier(KModifier.OVERRIDE)
                }
                val dataP = property("data" of numberArray) {
                    getter {
                        modifier(KModifier.EXTERNAL)
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
                    + "return \"Mat(\${factors { _, _, v -> v }.joinToString(\", \")})\""
                }

                val lig = function("lig", "l" of Int::class) { (l) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$l,$it]" }})"
                }

                val col = function("col", "c" of Int::class) { (c) ->
                    modifier(KModifier.OVERRIDE)

                    + "return ${Vec.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it,$c]" }})"
                }

                if (gen.deg == 4) {
                    val scale = function("scale", "f" of Vec3, returns = self) {
                        modifier(KModifier.EXTERNAL)
                    }
                    function(scale.name, "fx" of number, "fy" of number, "fz" of number, returns = self) { (fx, fy, fz) ->
                        + "return $scale($Vec3($fx, $fy, $fz))"
                    }

                    val translate = function("translate", "d" of Vec3, returns = self) {
                        modifier(KModifier.EXTERNAL)
                    }
                    function(translate.name, "dx" of number, "dy" of number, "dz" of number, returns = self) { (dx, dy, dz) ->
                        + "return $translate($Vec3($dx, $dy, $dz))"
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

                    + "return $self(this)"
                }
                operator("unaryMinus", returns = self) {
                    modifier(KModifier.OVERRIDE)

                    + "return $self(this).also { it *= -${gen.one} }"
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

                    + "return $self(this).also { it *= $k }"
                }

                operator("divAssign", "k" of number) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $divA($k)"
                }
                operator("div", "k" of number) { (k) ->
                    modifier(KModifier.OVERRIDE)

                    + "return $self(this).also { it /= $k }"
                }

                operator("times", "mat" of intf, returns = self) { (mat) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($mat is $self)") {
                        + "return $timesM($mat)"
                    }

                    + "return $self { l, c -> $lig(l) * $mat.$col(c) }"
                }

                operator("times", "v" of getInterface(gen, "Vector"), returns = Vec) { (v) ->
                    modifier(KModifier.OVERRIDE)

                    flow("if ($v is ${Vec.simpleName})") {
                        + "return $timesV($v)"
                    }

                    + "return ${Vec.simpleName} { $lig(it) * $v }"
                }
            }

            // Utils
            function("matrix", *matp, returns = Mat) {
                + "return $Mat(${matp.joinToString(", ")})"
            }

            if (gen.deg == 4) {
                function("scale", "fx" of number, "fy" of number, "fz" of number, returns = Mat) { (fx, fy, fz) ->
                    + "return $Mat.identity().scale($fx, $fy, $fz)"
                }
                function("scale", "f" of Vec3 , returns = Mat) { (f) ->
                    + "return $Mat.identity().scale($f)"
                }

                function("translate", "dx" of number, "dy" of number, "dz" of number, returns = Mat) { (dx, dy, dz) ->
                    + "return $Mat.identity().translate($dx, $dy, $dz)"
                }
                function("translate", "d" of Vec3, returns = Mat) { (d) ->
                    + "return $Mat.identity().translate($d)"
                }

                function("rotate", "a" of Double::class, "x" of number, "y" of number, "z" of number, returns = Mat) { (a, x, y, z) ->
                    + "return $Mat.rotate($a, $x, $y, $z)"
                }
                function("rotate", "a" of Double::class, "axe" of Vec3, returns = Mat) { (a, axe) ->
                    + "return $Mat.rotate($a, $axe)"
                }

                function("lookAt", "eye" of Vec3, "center" of Vec3, "up" of Vec3, returns = Mat) { (eye, center, up) ->
                    + "return $Mat.lookAt($eye, $center, $up)"
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}