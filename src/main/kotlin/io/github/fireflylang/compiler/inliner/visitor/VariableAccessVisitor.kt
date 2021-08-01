package io.github.fireflylang.compiler.inliner.visitor

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.base.VariableAccess
import com.github.jonathanxd.kores.modify.visit.PartVisitor
import com.github.jonathanxd.kores.modify.visit.VisitManager

object VariableAccessVisitor : PartVisitor<VariableAccess> {
    override fun visit(
        codePart: VariableAccess,
        data: TypedData,
        visitManager: VisitManager<*>
    ): VariableAccess {
        if (codePart.name.startsWith("__p")) {
            val map = VARIABLE_USAGE_COUNTER.getOrSet(data, mutableMapOf(codePart.name to 0L))
            map[codePart.name] = (map[codePart.name]!! + 1)
        }
        return codePart
    }
}