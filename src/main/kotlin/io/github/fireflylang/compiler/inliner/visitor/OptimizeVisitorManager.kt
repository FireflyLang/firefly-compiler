package io.github.fireflylang.compiler.inliner.visitor

import com.github.jonathanxd.kores.modify.visit.PartVisitor
import com.github.jonathanxd.kores.modify.visit.VisitManager

class OptimizeVisitorManager<T: Any> : VisitManager<T>() {
    override fun <U : Any> getVisitor(type: Class<*>): PartVisitor<U> {
        try {
            return super.getVisitor(type) ?: EmptyVisitor()
        } catch (t: Throwable) {
            t.printStackTrace()
            return EmptyVisitor()
        }
    }
}