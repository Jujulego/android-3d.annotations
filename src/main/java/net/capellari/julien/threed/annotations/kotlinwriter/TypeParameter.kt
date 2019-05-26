package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import kotlin.reflect.KClass

@KotlinMarker
class TypeParameter(private val name: String): Modifiable {
    // Attributs
    private var bounds = arrayOf<TypeName>()
    private var variance: KModifier? = null
    private var isReified = false

    // Property
    val spec get() = TypeVariableName(name, *bounds, variance = variance)

    // Méthodes
    // - modifiers
    override fun modifiers(vararg modifiers: KModifier) {
        for (modifier in modifiers) {
            when (modifier) {
                KModifier.IN  -> variance = KModifier.IN
                KModifier.OUT -> variance = KModifier.OUT

                KModifier.REIFIED -> isReified = true

                else -> {}
            }
        }
    }

    // - bounds
    fun bound(type: TypeName) {
        bounds += type
    }
    fun bound(type: KClass<*>, nullable: Boolean = false) {
        bounds += type.asNullableTypeName(nullable)
    }
}