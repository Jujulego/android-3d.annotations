package net.capellari.julien.threed.annotations

@Target(AnnotationTarget.CLASS)
annotation class Program(
    val shaders: Array<ShaderScript>
)