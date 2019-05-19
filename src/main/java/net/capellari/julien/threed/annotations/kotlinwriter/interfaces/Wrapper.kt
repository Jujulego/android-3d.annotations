package net.capellari.julien.threed.annotations.kotlinwriter.interfaces

interface Wrapper<out S,out B> {
    // Attributs
    val builder: B
    val spec: S
}