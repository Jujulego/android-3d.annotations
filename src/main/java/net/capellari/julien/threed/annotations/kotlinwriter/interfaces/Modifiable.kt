package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import com.squareup.kotlinpoet.KModifier

interface Modifiable {
    // Méthodes
    fun modifiers(vararg modifiers: KModifier)
}