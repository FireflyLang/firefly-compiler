package io.github.fireflylang.compiler.inliner.visitor.optimizer

import com.github.jonathanxd.iutils.`object`.TypedKey
import com.github.jonathanxd.iutils.kt.typeInfo
import com.github.jonathanxd.kores.Instruction

val OPTIMIZE_VARS = TypedKey<Map<String, Instruction>>("OPTIMIZE_VARS", typeInfo())