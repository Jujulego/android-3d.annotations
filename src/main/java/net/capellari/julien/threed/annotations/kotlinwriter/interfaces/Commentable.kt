package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

interface Commentable {
    // Méthodes
    // - comment
    fun comment(format: String, vararg args: Any)
}