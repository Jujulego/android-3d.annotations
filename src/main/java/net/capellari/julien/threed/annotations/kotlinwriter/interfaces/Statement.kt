package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

interface Statement<out S, out B>: Wrapper<S,B> {
    // Opérateurs
    operator fun String.unaryPlus() {
        this@Statement.format(this)
    }

    // Méthodes
    // - code
    fun format(format: String, vararg args: Any)
}