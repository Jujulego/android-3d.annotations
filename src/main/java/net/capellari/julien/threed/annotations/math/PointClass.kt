package net.capellari.julien.threed.annotations.math

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PointClass(
    val type: NumberType,
    val deg: Int
)