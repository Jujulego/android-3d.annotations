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
class PointGenerator(processingEnv: ProcessingEnvironment): AbsGenerator(processingEnv) {
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
        val clsName = ClassName(pkg, getName(gen, "Point"))

        val number = gen.kcls
        val numberArray = gen.karray

        val baseName = base.asClassName()
            .parameterizedBy(number.asTypeName())

        // Generate class
        val code = createFile(pkg, clsName.simpleName) {
            // Classe
            addClass(clsName) {
                // superclass
                superclass("net.capellari.julien.threed.jni", "JNIClass")
                superclassParameter("handle")

                // interface
                superinterface(baseName)
                superinterface(getInterface(gen, "Point"))

                // Companion
                companion {
                    function("create") {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)

                        parameters(getCoordParameters(gen))
                        returns<Long>()
                    }

                    function("createA", "factors" of numberArray, returns = Long::class) {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    }

                    function("createC", "v" of clsName, returns = Long::class) {
                        annotation<JvmStatic>()
                        modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
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
                    parameter("pt", clsName)
                    callThis("createC(pt)")
                }

                // Opérateurs
                get("i" of Int::class, returns = number) {
                    + "return getCoord(i)"
                }

                set("i" of Int::class, "v" of number) {
                    + "return setCoord(i, v)"
                }

                unaryPlus(returns = clsName) {
                    + "return ${clsName.simpleName}(this)"
                }

                unaryMinus(returns = clsName) {
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "-this[$it]" }})"
                }

                plusAssign("v" of getInterface(gen, "Vector")) {
                    for (i in 0 until gen.deg) {
                        + "this[$i] += v[$i]"
                    }
                }

                minusAssign("v" of getInterface(gen, "Vector")) {
                    for (i in 0 until gen.deg) {
                        + "this[$i] -= v[$i]"
                    }
                }

                plus("v" of getInterface(gen, "Vector"), returns = clsName) {
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] + v[$it]" }})"
                }

                minus("v" of getInterface(gen, "Vector"), returns = clsName) {
                    + "return ${clsName.simpleName}(${(0 until gen.deg).joinToString(", ") { "this[$it] - v[$it]" }})"
                }

                minus("pt" of getInterface(gen, "Point"), returns = ClassName(pkg, getName(gen, "Vec"))) {
                    + "return ${getName(gen, "Vec")}(${(0 until gen.deg).joinToString(", ") { "this[$it] - pt[$it]" }})"
                }

                times("c" of getInterface(gen, "Coord"), returns = number) {
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
                    + "return \"Point(${(0 until gen.deg).joinToString(", ") { "\${this[$it]}" }})\""
                }

                // Méthodes natives
                function("getDataA", returns = numberArray) {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                function("getCoord", "i" of Int::class, returns = number) {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                function("setCoord", "i" of Int::class, "v" of number) {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                }

                function("equal", "other" of clsName, returns = Boolean::class) {
                    modifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                }
            }

            // Utils
            function("point") {
                parameters(getCoordParameters(gen))
                returns(clsName)

                + "return $clsName(${(0 until gen.deg).joinToString(", ") { "v$it" }})"
            }
        }

        utils.writeTo(utils.sourceDir, code.spec)
    }
}