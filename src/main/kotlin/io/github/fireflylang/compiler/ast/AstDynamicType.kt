package io.github.fireflylang.compiler.ast

import com.github.jonathanxd.kores.type.typeOf
import io.github.fireflylang.std.dynamic
import java.lang.reflect.Type

val AstDynamicType get(): Type = typeOf<`dynamic`>()
