package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

interface Commentable<out S, out B>: Wrapper<S,B> {
    // Méthodes
    // - comment
    fun comment(format: String, vararg args: Any)
}