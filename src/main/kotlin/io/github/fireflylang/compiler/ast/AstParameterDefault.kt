package io.github.fireflylang.compiler.ast

import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.base.KoresParameter

data class AstParameterDefault(val parameter: KoresParameter, val defaults: MutableList<Instruction>)