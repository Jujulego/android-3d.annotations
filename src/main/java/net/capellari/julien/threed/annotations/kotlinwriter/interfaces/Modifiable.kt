package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.KModifier

interface Modifiable<out S, out B> : Wrapper<S,B> {
    // Méthodes
    fun modifiers(vararg modifiers: KModifier)
}