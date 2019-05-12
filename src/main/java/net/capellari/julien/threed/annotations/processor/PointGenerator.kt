package net.capellari.julien.threed.annotations.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
                superclass(ClassName("net.capellari.julien.threed.jni", "JNIClass"))
                addSuperclassConstructorParameter("handle", Long::class)

                // interface
                addSuperinterface(baseName)
                addSuperinterface(getInterface(point, "Point"))

                // Companion
                addCompanion {
                    addFunction("create") {
                        addAnnotation(JvmStatic::class)
                        addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                        addParameters(getCoordParameters(point))
                        returns(Long::class)
                    }

                    addFunction("createA") {
                        addAnnotation(JvmStatic::class)
                        addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                        addParameter("factors", numberArray)
                        returns(Long::class)
                    }

                    addFunction("createC") {
                        addAnnotation(JvmStatic::class)
                        addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                        addParameter("pt", clsName)
                        returns(Long::class)
                    }
                }

                // Propriétés
                addProperty("data", numberArray) {
                    getter {
                        addStatement("return getDataA()")
                    }
                }

                // Constructeurs
                addPrimaryConstructor {
                    addModifiers(KModifier.INTERNAL)
                    addParameter("handle", Long::class)
                }

                addConstructor {
                    callThisConstructor("create(0, 0)")
                }

                addConstructor {
                    addParameters(getCoordParameters(point))

                    callThisConstructor {
                        add("create(")
                        add((0 until point.deg).map { "v$it" }.joinToString(", "))
                        add(")")
                    }
                }

                addConstructor {
                    addParameter("factors", numberArray)
                    callThisConstructor("createA(factors)")
                }

                addConstructor {
                    addParameter("pt", clsName)
                    callThisConstructor("createC(pt)")
                }

                // Opérateurs
                addFunction("get") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("i", Int::class)
                    returns(number)

                    addStatement("return getCoord(i)")
                }

                addFunction("set") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("i", Int::class)
                    addParameter("v", number)

                    addStatement("return setCoord(i, v)")
                }

                addFunction("unaryPlus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    returns(clsName)

                    addCode {
                        add("return ${clsName.simpleName}(")
                        add((0 until point.deg).map { "+this[$it]" }.joinToString(", "))
                        add(")")
                    }
                }

                addFunction("unaryMinus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    returns(clsName)

                    addCode {
                        add("return ${clsName.simpleName}(")
                        add((0 until point.deg).map { "-this[$it]" }.joinToString(", "))
                        add(")")
                    }
                }

                addFunction("plusAssign") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))

                    for (i in 0 until point.deg) {
                        addStatement("this[$i] += v[$i]")
                    }
                }

                addFunction("minusAssign") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))

                    for (i in 0 until point.deg) {
                        addStatement("this[$i] -= v[$i]")
                    }
                }

                addFunction("plus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))
                    returns(clsName)

                    addCode {
                        add("return ${clsName.simpleName}(")
                        add((0 until point.deg).map { "this[$it] + v[$it]" }.joinToString(", "))
                        add(")")
                    }
                }

                addFunction("minus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("v", getInterface(point, "Vector"))
                    returns(clsName)

                    addCode {
                        add("return ${clsName.simpleName}(")
                        add((0 until point.deg).map { "this[$it] - v[$it]" }.joinToString(", "))
                        add(")")
                    }
                }

                addFunction("minus") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("pt", getInterface(point, "Point"))
                    returns(ClassName(pkg, getName(point, "Vec")))

                    addCode {
                        add("return ${getName(point, "Vec")}(")
                        add((0 until point.deg).map { "this[$it] - pt[$it]" }.joinToString(", "))
                        add(")")
                    }
                }

                addFunction("times") {
                    addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                    addParameter("c", getInterface(point, "Coord"))
                    returns(number)

                    addCode {
                        add("return ")
                        add((0 until point.deg).map { "(this[$it] * c[$it])" }.joinToString(" + "))
                    }
                }

                // Méthodes
                addFunction("equals") {
                    addModifiers(KModifier.OVERRIDE)
                    addParameter("other", Any::class.asTypeName().copy(nullable = true))
                    returns(Boolean::class)

                    addCode {
                        add("""
                            if (other === this) return true
                            if (other is ${clsName.simpleName}) return equal(other)

                            return super.equals(other)
                        """.trimIndent())
                    }
                }

                addFunction("hashCode") {
                    addModifiers(KModifier.OVERRIDE)
                    returns(Int::class)

                    addStatement("return data.contentHashCode()")
                }

                addFunction("toString") {
                    addModifiers(KModifier.OVERRIDE)
                    returns(String::class)

                    addCode {
                        add("return \"Point(")
                        add((0 until point.deg).map { "\${this[$it]}" }.joinToString(", "))
                        add(")\"")
                    }
                }

                // Méthodes natives
                addFunction("getDataA") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    returns(numberArray)
                }

                addFunction("getCoord") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    addParameter("i", Int::class)
                    returns(number)
                }

                addFunction("setCoord") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    addParameter("i", Int::class)
                    addParameter("v", number)
                }

                addFunction("equal") {
                    addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    addParameter("other", clsName)
                    returns(Boolean::class)
                }
            }
        }

        utils.writeTo(utils.sourceDir, code)
    }
}