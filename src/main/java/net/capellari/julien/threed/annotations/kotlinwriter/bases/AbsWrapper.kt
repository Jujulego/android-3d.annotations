package net.capellari.julien.threed.annotations.kotlinwriter.bases

import net.capellari.julien.threed.annotations.kotlinwriter.interfaces.Wrapper

abstract class AbsWrapper<S,B>(override val builder: B) : Wrapper<S,B>