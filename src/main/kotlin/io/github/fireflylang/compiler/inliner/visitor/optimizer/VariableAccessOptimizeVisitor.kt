package io.github.fireflylang.compiler.inliner.visitor.optimizer

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.base.VariableAccess
import com.github.jonathanxd.kores.modify.visit.PartVisitor
import com.github.jonathanxd.kores.modify.visit.VisitManager

object VariableAccessOptimizeVisitor : PartVisitor<Instruction> {
    override fun visit(
        codePart: Instruction,
        data: TypedData,
        visitManager: VisitManager<*>
    ): Instruction {
        if (codePart is VariableAccess) {
            if (codePart.name.startsWith("__p")) {
                val vars = OPTIMIZE_VARS.getOrNull(data)
                if (vars != null) {
                    val replacement = vars[codePart.name]
                    if (replacement != null) {
                        return replacement
                    }
                }
            }
        }

        return codePart
    }
}