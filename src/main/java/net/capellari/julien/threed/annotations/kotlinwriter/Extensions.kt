package net.capellari.julien.threed.annotations.kotlinwriter

fun createFile(pkg: String, name: String, build: File.() -> Unit) = File(pkg, name).apply(build)