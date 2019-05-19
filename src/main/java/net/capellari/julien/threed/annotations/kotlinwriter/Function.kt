package net.capellari.julien.threed.annotations.kotlinwriter

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import net.capellari.julien.threed.annotations.kotlinwriter.bases.AbsFunction
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Parameters
import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Returns
import kotlin.reflect.KClass

class Function(name: String): AbsFunction(FunSpec.builder(name)), Parameters, Returns