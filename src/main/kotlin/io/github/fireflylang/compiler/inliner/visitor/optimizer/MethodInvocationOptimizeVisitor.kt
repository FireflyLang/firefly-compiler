package io.github.fireflylang.compiler.inliner.visitor.optimizer

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.base.MethodInvocation
import com.github.jonathanxd.kores.modify.visit.PartVisitor
import com.github.jonathanxd.kores.modify.visit.VisitManager

object MethodInvocationOptimizeVisitor : PartVisitor<MethodInvocation> {
    override fun visit(
        codePart: MethodInvocation,
        data: TypedData,
        visitManager: VisitManager<*>
    ): MethodInvocation = codePart.copy(arguments = codePart.arguments.map {
        visitManager.visit(it, data)
    })
}