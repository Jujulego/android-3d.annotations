package net.capellari.julien.threed.annotations.math

@Target(AnnotationTarget.CLASS)
annotation class PointClass(
    val type: NumberType,
    val deg: Int
)