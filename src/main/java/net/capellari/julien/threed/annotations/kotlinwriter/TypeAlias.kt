package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Modifiable
import kotlin.reflect.KClass

@KotlinMarker
class TypeAlias: AbsWrapper<TypeAliasSpec, TypeAliasSpec.Builder>,
        Modifiable {

    // Propriétés
    override val spec get() = builder.build()

    // Constructeur
    constructor(name: String, type: TypeName): super(TypeAliasSpec.builder(name, type))
    constructor(name: String, type: KClass<*>): super(TypeAliasSpec.builder(name, type))

    // Méthodes
    // - modifiers
    override fun modifiers(vararg modifiers: KModifier) {
        builder.addModifiers(*modifiers)
    }
}