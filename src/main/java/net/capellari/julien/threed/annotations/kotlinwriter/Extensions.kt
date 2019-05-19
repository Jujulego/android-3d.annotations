package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.asTypeName
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns

// File
fun createFile(pkg: String, name: String, build: File.() -> Unit) = File(pkg, name).apply(build)

// Type shortcut
inline fun <reified T: Any> Parameters.addParameter(name: String, nullable: Boolean = false) {
    if (nullable) {
        addParameter(name, T::class.asTypeName().copy(nullable = true))
    }

    addParameter(name, T::class)
}

inline fun <reified T: Any> Returns.returns() = returns(T::class)