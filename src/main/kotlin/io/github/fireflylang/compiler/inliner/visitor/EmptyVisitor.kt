package io.github.fireflylang.compiler.inliner.visitor

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.modify.visit.PartVisitor
import com.github.jonathanxd.kores.modify.visit.VisitManager

class EmptyVisitor<U: Any> : PartVisitor<U> {
    override fun visit(codePart: U, data: TypedData, visitManager: VisitManager<*>): U =
        codePart

}