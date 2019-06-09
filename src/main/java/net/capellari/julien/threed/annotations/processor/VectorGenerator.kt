package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.threed.annotations.kotlinwriter.*
import net.capellari.julien.threed.annotations.math.Generator
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@RequiresApi(26)
class VectorGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
    // Functions
    private fun getName(gen: Generator, t: String): String {
        return "$t${gen.deg}${gen.identifier}"
    }

    private fun getCoordParameters(gen: Generator): List<Parameter> {
        val type = gen.kcls
        val params = mutableListOf<Parameter>()

        for (i in 0 until gen.deg) {
            params.add(Parameter("v$i", type))
        }

        return params
    }

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

        // Generate class
        val code = createFile(pkg, clsName.simpleName) {
            addClass(clsName) {
                // superclass
                superclass("net.capellari.julien.threed.jni", "JNIClass")
                superclassParameter("handle")

                // interface
                superinterface(baseName)
                superinterface(getInterface(gen, "Vector"))

                // Companion
                companion {
                    function("create") {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        parameters(getCoordParameters(gen))
                        returns<Long>()
                    }

                    function("createA") {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        parameter("factors", numberArray)
                        returns<Long>()
                    }

                    function("createC") {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        parameter("v", clsName)
                        returns<Long>()
                    }
                }

                // Propriétés
                property("data", numberArray) {
                    getter {
                        + "return getDataA()"
                    }
                }

                // Constructeurs
                primaryConstructor {
                    modifiers(KModifier.INTERNAL)
                    parameter<Long>("handle")
                }

                constructor {
                    callThis("create(${(0 until gen.deg).joinToString(", ") { gen.zero }})")
                }

                constructor {
                    parameters(getCoordParameters(gen))
                    callThis("create(${(0 until gen.deg).joinToString(", ") { "v$it" }})")
                }

                constructor {
                    parameter("factors", numberArray)
                    callThis("createA(factors)")
                }

                constructor {
                    parameter("gen", genGeneratorType(gen))
                    callThis("${gen.array_name}(${gen.deg}, gen)")
                }

                constructor {
                    parameter("v", clsName)
                    callThis("createC(v)")
                }

                // Opérateurs
                function("get") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter<Int>("i")
                    returns(number)

                    + "return getCoord(i)"
                }

                function("set") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter<Int>("i")
                    parameter("v", number)

                    + "return setCoord(i, v)"
                }

                function("unaryPlus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    returns(clsName)

                    + "return ${clsName.simpleName}(this)"
                }

                function("unaryMinus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    returns(clsName)

                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "-this[$it]" }})"
                }

                function("plusAssign") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(gen, "Vector"))

                    for (i in 0 until gen.deg) {
                        + "this[$i] += v[$i]"
                    }
                }

                function("minusAssign") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(gen, "Vector"))

                    for (i in 0 until gen.deg) {
                        + "this[$i] -= v[$i]"
                    }
                }

                function("timesAssign") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("k", gen.kcls)

                    for (i in 0 until gen.deg) {
                        + "this[$i] *= k"
                    }
                }

                function("divAssign") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("k", gen.kcls)

                    for (i in 0 until gen.deg) {
                        + "this[$i] /= k"
                    }
                }

                function("plus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(gen, "Vector"))
                    returns(clsName)

                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] + v[$it]" }})"
                }

                function("minus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(gen, "Vector"))
                    returns(clsName)

                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] - v[$it]" }})"
                }

                function("times") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("k", gen.kcls)
                    returns(clsName)

                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] * k" }})"
                }

                function("div") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("k", gen.kcls)
                    returns(clsName)

                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] / k" }})"
                }

                function("times") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("c", getInterface(gen, "Coord"))
                    returns(number)

                    + "return ${(0 until gen.deg).joinToString(" + ") { "(this[$it] * c[$it])" }}"
                }

                // Méthodes
                override(Any::equals) {
                    flow("if (other === this)") {
                        + "return true"
                    }.end()

                    flow("if (other is ${clsName.simpleName})") {
                        + "return equal(other)"
                    }.end()

                    + "return super.equals(other)"
                }

                override(Any::hashCode) {
                    + "return data.contentHashCode()"
                }

                override(Any::toString) {
                    + "return \"Vec(${(0 until gen.deg).joinToString(", ") { "\${this[$it]}" }})\""
                }

                if (gen.deg == 3) {
                    function("vect") {
                        modifiers(KModifier.INFIX)
                        parameter("v", clsName)
                        returns(clsName)

                        + "return $clsName(this[1] * v[2] - this[2] * v[1], this[0] * v[2] - this[2] * v[0], this[0] * v[1] - this[1] * v[0])"
                    }
                }

                // Méthodes natives
                function("getDataA") {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    returns(numberArray)
                }

                function("getCoord") {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    parameter<Int>("i")
                    returns(number)
                }

                function("setCoord") {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    parameter<Int>("i")
                    parameter("v", number)
                }

                function("equal") {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    parameter("other", clsName)
                    returns<Boolean>()
                }
            }

            function("vector") {
                parameters(getCoordParameters(gen))
                returns(clsName)

                + ("return $clsName(${(0 until gen.deg).joinToString(", ") { "v$it" }})")
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}