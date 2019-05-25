package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsWrapper
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Annotable
import kotlin.reflect.KClass

@KotlinMarker
class File(pkg: String, name: String):
        AbsWrapper<FileSpec,FileSpec.Builder>(FileSpec.builder(pkg, name)),
        Annotable<FileSpec,FileSpec.Builder> {

    // Propriétés
    override val spec get() = builder.build()

    // Méthodes
    // - annotation
    override fun annotation(type: ClassName) {
        builder.addAnnotation(type)
    }
    override fun annotation(type: KClass<*>) {
        builder.addAnnotation(type)
    }

    // - imports
    fun import(pkg: String, name: String, alias: String? = null) {
        if (alias == null) {
            builder.addImport(pkg, name)
        } else {
            builder.addAliasedImport(MemberName(pkg, name), alias)
        }
    }
    fun imports(pkg: String, vararg names: Pair<String,String?>) {
        for (name in names) {
            import(pkg, name.first, name.second)
        }
    }

    fun import(name: ClassName, alias: String? = null) {
        if (alias == null) {
            builder.addImport(name.packageName, name.simpleName)
        } else {
            builder.addAliasedImport(name, alias)
        }
    }
    fun import(cls: KClass<*>, alias: String? = null) {
        if (alias == null) {
            val name = cls.asClassName()
            builder.addImport(name.packageName, name.simpleName)
        } else {
            builder.addAliasedImport(cls, alias)
        }
    }

    // - types
    fun addClass(name: ClassName, build: Class.() -> Unit)
            = Class(name).apply(build).spec.also { builder.addType(it) }

    fun addClass(name: String, build: Class.() -> Unit)
            = Class(name).apply(build).spec.also { builder.addType(it) }

    fun addEnum(name: ClassName, build: Enum.() -> Unit)
            = Enum(name).apply(build).spec.also { builder.addType(it) }

    fun addEnum(name: String, build: Enum.() -> Unit)
            = Enum(name).apply(build).spec.also { builder.addType(it) }

    fun addObject(name: ClassName, build: Object.() -> Unit)
            = Object(name).apply(build).spec.also { builder.addType(it) }

    fun addObject(name: String, build: Object.() -> Unit)
            = Object(name).apply(build).spec.also { builder.addType(it) }
}