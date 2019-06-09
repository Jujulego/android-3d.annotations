package net.capellari.julien.threed.annotations.math

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Generate(
    vararg val generators: Generator
)