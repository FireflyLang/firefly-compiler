package io.github.fireflylang.compiler.inliner.visitor

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.base.MethodInvocation
import com.github.jonathanxd.kores.modify.visit.PartVisitor
import com.github.jonathanxd.kores.modify.visit.VisitManager

object MethodInvocationVisitor : PartVisitor<MethodInvocation> {
    override fun visit(
        codePart: MethodInvocation,
        data: TypedData,
        visitManager: VisitManager<*>
    ): MethodInvocation {
        codePart.arguments.forEach {
            visitManager.visit(it, data)
        }
        return codePart
    }
}