package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.threed.annotations.kotlinwriter.*
import net.capellari.julien.threed.annotations.math.NumberType
import net.capellari.julien.threed.annotations.math.PointClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

@RequiresApi(26)
class PointGenerator(processingEnv: ProcessingEnvironment) {
    // Propriétés
    private val utils = Utils(processingEnv)

    // Opérateur
    operator fun invoke(it: Element) {
        if (it is TypeElement) {
            generate(it)
        }
    }

    // Functions
    private fun getName(point: PointClass, t: String): String {
        return "$t${point.deg}" +
                when(point.type) {
                    NumberType.INT   -> "i"
                    NumberType.FLOAT -> "f"
                }
    }

    private fun getNumberType(point: PointClass): KClass<*> {
        return when(point.type) {
            NumberType.INT   -> Int::class
            NumberType.FLOAT -> Float::class
        }
    }

    private fun getNumberArrayType(point: PointClass): KClass<*> {
        return when(point.type) {
            NumberType.INT   -> IntArray::class
            NumberType.FLOAT -> FloatArray::class
        }
    }

    private fun getCoordParameters(point: PointClass): List<Parameter> {
        val type = getNumberType(point)
        val params = mutableListOf<Parameter>()

        for (i in 0 until point.deg) {
            params.add(Parameter("v$i", type))
        }

        return params
    }

    private fun getInterface(point: PointClass, name: String): TypeName {
        return ClassName("net.capellari.julien.threed.math", name)
            .parameterizedBy(
                getNumberType(point).asTypeName(),
                ClassName("net.capellari.julien.threed.math", "D${point.deg}")
            )
    }

    private fun generate(base: TypeElement) {
        // Get infos
        val pkg = "net.capellari.julien.threed"
        val point = base.getAnnotation<PointClass>()
        val clsName = ClassName(pkg, getName(point, "Point"))

        val number = getNumberType(point)
        val numberArray = getNumberArrayType(point)

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
                superinterface(getInterface(point, "Point"))

                // Companion
                companion {
                    function("create") {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        parameters(getCoordParameters(point))
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

                        parameter("pt", clsName)
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
                    callThis("create(0, 0)")
                }

                constructor {
                    parameters(getCoordParameters(point))

                    callThis("create(" + (0 until point.deg).joinToString(", ") { "v$it" } + ")")
                }

                constructor {
                    parameter("factors", numberArray)
                    callThis("createA(factors)")
                }

                constructor {
                    parameter("pt", clsName)
                    callThis("createC(pt)")
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

                    + ("return ${clsName.simpleName}(" + (0 until point.deg).joinToString(", ") { "-this[$it]" } + ")")
                }

                function("plusAssign") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(point, "Vector"))

                    for (i in 0 until point.deg) {
                        + "this[$i] += v[$i]"
                    }
                }

                function("minusAssign") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(point, "Vector"))

                    for (i in 0 until point.deg) {
                        + "this[$i] -= v[$i]"
                    }
                }

                function("plus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(point, "Vector"))
                    returns(clsName)

                    + ("return ${clsName.simpleName}(" + (0 until point.deg).joinToString(", ") { "this[$it] + v[$it]" } + ")")
                }

                function("minus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("v", getInterface(point, "Vector"))
                    returns(clsName)

                    + ("return ${clsName.simpleName}(" + (0 until point.deg).joinToString(", ") { "this[$it] - v[$it]" } + ")")
                }

                function("minus") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("pt", getInterface(point, "Point"))
                    returns(ClassName(pkg, getName(point, "Vec")))

                    + ("return ${getName(point, "Vec")}(" + (0 until point.deg).joinToString(", ") { "this[$it] - pt[$it]" } + ")")
                }

                function("times") {
                    modifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    parameter("c", getInterface(point, "Coord"))
                    returns(number)

                    + ("return " + (0 until point.deg).joinToString(" + ") { "(this[$it] * c[$it])" })
                }

                // Méthodes
                function("equals") {
                    modifiers(KModifier.OVERRIDE)
                    parameter<Any>("other", nullable = true)
                    returns<Boolean>()

                    + "if (other === this) return true"
                    + "if (other is ${clsName.simpleName}) return equal(other)"
                    + ""
                    + "return super.equals(other)"
                }

                function("hashCode") {
                    modifiers(KModifier.OVERRIDE)
                    returns<Int>()

                    + "return data.contentHashCode()"
                }

                function("toString") {
                    modifiers(KModifier.OVERRIDE)
                    returns<String>()

                    + ("return \"Point(" + (0 until point.deg).joinToString(", ") { "\${this[$it]}" } + ")\"")
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
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}