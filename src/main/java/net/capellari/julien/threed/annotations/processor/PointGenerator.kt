package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.capellari.julien.threed.annotations.kotlinwriter.addParameter
import net.capellari.julien.threed.annotations.kotlinwriter.createFile
import net.capellari.julien.threed.annotations.kotlinwriter.returns
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

    private fun getCoordParameters(point: PointClass): List<ParameterSpec> {
        val type = getNumberType(point)
        val params = mutableListOf<ParameterSpec>()

        for (i in 0 until point.deg) {
            params.add(ParameterSpec.builder("v$i", type).build())
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
                addSuperclassParameter("handle")

                // interface
                addSuperinterface(baseName)
                addSuperinterface(getInterface(point, "Point"))

                // Companion
                companion {
                    addFunction("create") {
                        addAnnotation<JvmStatic>()
                        addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        builder.addParameters(getCoordParameters(point))
                        returns<Long>()
                    }

                    addFunction("createA") {
                        addAnnotation<JvmStatic>()
                        addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        addParameter("factors", numberArray)
                        returns<Long>()
                    }

                    addFunction("createC") {
                        addAnnotation<JvmStatic>()
                        addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        addParameter("pt", clsName)
                        returns<Long>()
                    }
                }

                // Propriétés
                addProperty("data", numberArray) {
                    getter {
                        + "return getDataA()"
                    }
                }

                // Constructeurs
                primaryConstructor {
                    addModifiers(KModifier.INTERNAL)
                    addParameter("handle", Long::class)
                }

                addConstructor {
                    callThis("create(0, 0)")
                }

                addConstructor {
                    builder.addParameters(getCoordParameters(point))

                    callThis("create(" + (0 until point.deg).map { "v$it" }.joinToString(", ") + ")")
                }

                addConstructor {
                    addParameter("factors", numberArray)
                    callThis("createA(factors)")
                }

                addConstructor {
                    addParameter("pt", clsName)
                    callThis("createC(pt)")
                }

                // Opérateurs
                addFunction("get") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("i", Int::class)
                    returns(number)

                    + "return getCoord(i)"
                }

                addFunction("set") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("i", Int::class)
                    addParameter("v", number)

                    + "return setCoord(i, v)"
                }

                addFunction("unaryPlus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    returns(clsName)

                    + "return ${clsName.simpleName}(this)"
                }

                addFunction("unaryMinus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    returns(clsName)

                    + ("return ${clsName.simpleName}(" + (0 until point.deg).joinToString(", ") { "-this[$it]" } + ")")
                }

                addFunction("plusAssign") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))

                    for (i in 0 until point.deg) {
                        + "this[$i] += v[$i]"
                    }
                }

                addFunction("minusAssign") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))

                    for (i in 0 until point.deg) {
                        + "this[$i] -= v[$i]"
                    }
                }

                addFunction("plus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))
                    returns(clsName)

                    + ("return ${clsName.simpleName}(" + (0 until point.deg).joinToString(", ") { "this[$it] + v[$it]" } + ")")
                }

                addFunction("minus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))
                    returns(clsName)

                    + ("return ${clsName.simpleName}(" + (0 until point.deg).joinToString(", ") { "this[$it] - v[$it]" } + ")")
                }

                addFunction("minus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("pt", getInterface(point, "Point"))
                    returns(ClassName(pkg, getName(point, "Vec")))

                    + ("return ${getName(point, "Vec")}(" + (0 until point.deg).joinToString(", ") { "this[$it] - pt[$it]" } + ")")
                }

                addFunction("times") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("c", getInterface(point, "Coord"))
                    returns(number)

                    + ("return " + (0 until point.deg).joinToString(" + ") { "(this[$it] * c[$it])" })
                }

                // Méthodes
                addFunction("equals") {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter<Any>("other", nullable = true)
                    returns<Boolean>()

                    + "if (other === this) return true"
                    + "if (other is ${clsName.simpleName}) return equal(other)"
                    + ""
                    + "return super.equals(other)"
                }

                addFunction("hashCode") {
                    addModifiers(KModifier.OVERRIDE)
                    returns<Int>()

                    + "return data.contentHashCode()"
                }

                addFunction("toString") {
                    addModifiers(KModifier.OVERRIDE)
                    returns<String>()

                    + ("return \"Point(" + (0 until point.deg).joinToString(", ") { "\${this[$it]}" } + ")\"")
                }

                // Méthodes natives
                addFunction("getDataA") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    returns(numberArray)
                }

                addFunction("getCoord") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    addParameter<Int>("i")
                    returns(number)
                }

                addFunction("setCoord") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    addParameter<Int>("i")
                    addParameter("v", number)
                }

                addFunction("equal") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    addParameter("other", clsName)
                    returns<Boolean>()
                }
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}