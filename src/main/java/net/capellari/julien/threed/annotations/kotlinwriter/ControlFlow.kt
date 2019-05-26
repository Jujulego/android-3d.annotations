package net.capellari.julien.threed.annotations.kotlinwriter

import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Codable

// Classe
class ControlFlow<T: Codable<T>>(
        val code: T,
        val format: String,
        val args: Array<out Any>,
        val build: T.() -> Unit,
        val previous: ControlFlow<T>? = null
    ) {

    // Opérateurs
    operator fun invoke(format: String, vararg args: Any, build: T.() -> Unit)
            = next(format, *args, build = build)

    // Méthodes
    private fun build(last: Boolean) {
        if (previous == null) {
            code.beginFlow(format, *args)
        } else {
            previous.build(false)
            code.nextFlow(format, *args)
        }

        code.apply(build)

        if (last) {
            code.endFlow()
        }
    }

    fun next(format: String, vararg args: Any, build: T.() -> Unit)
            = ControlFlow(code, format, args, build, this)

    fun end() = build(true)
}