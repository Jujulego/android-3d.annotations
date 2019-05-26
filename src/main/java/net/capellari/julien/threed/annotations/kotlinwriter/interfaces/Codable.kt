package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

import net.capellari.julien.threed.annotations.kotlinwriter.ControlFlow

interface Codable<T: Codable<T>>: Commentable {
    // Opérateurs
    operator fun String.unaryPlus() {
        this@Codable.format(this)
    }

    // Méthodes
    // - code
    fun format(format: String, vararg args: Any)

    // - internal
    fun beginFlow(format: String, vararg args: Any)
    fun nextFlow(format: String, vararg args: Any)
    fun endFlow()

    @Suppress("UNCHECKED_CAST")
    fun flow(format: String, vararg args: Any, build: T.() -> Unit) = ControlFlow(this as T, format, args, build)
}