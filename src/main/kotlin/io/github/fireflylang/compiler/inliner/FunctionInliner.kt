/**
 *      firefly-compiler - Firefly Language parser and compiler.
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) JonathanxD <https://github.com/JonathanxD/>
 *      Copyright (c) contributors
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package io.github.fireflylang.compiler.inliner

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.base.MethodInvocation
import com.github.jonathanxd.kores.base.VariableAccess
import com.github.jonathanxd.kores.base.VariableDeclaration
import com.github.jonathanxd.kores.factory.variable
import com.github.jonathanxd.kores.modify.visit.VisitManager
import io.github.fireflylang.compiler.inliner.visitor.MethodInvocationVisitor
import io.github.fireflylang.compiler.inliner.visitor.OptimizeVisitorManager
import io.github.fireflylang.compiler.inliner.visitor.VARIABLE_USAGE_COUNTER
import io.github.fireflylang.compiler.inliner.visitor.VariableAccessVisitor
import io.github.fireflylang.compiler.inliner.visitor.optimizer.MethodInvocationOptimizeVisitor
import io.github.fireflylang.compiler.inliner.visitor.optimizer.OPTIMIZE_VARS
import io.github.fireflylang.compiler.inliner.visitor.optimizer.VariableAccessOptimizeVisitor

interface FunctionInliner {

    fun inline(
        args: List<Instruction>,
        declaration: MethodDeclaration
    ): List<Instruction>
}

/**
 * Simple inlining logic.
 *
 * A logic based in `modify.visit` module of **Kores** would be very nice, as it would allow
 * us to rename variables in their usage point.
 *
 * We also need to decide how we will achieve inlining external functions, would it be directly through ASM
 * using `*load` offsets? It looks appropriate to me.
 * Or we store `MethodDeclaration` compressed in the class? It provides a more powerful way to do inlining and
 * manipulation without needing to mix ASM and **Kores AST**.
 */
class StandardFunctionInliner: FunctionInliner {
    private val counterVisitorManager = OptimizeVisitorManager<Instruction>()
    private val optimizeVisitorManager = OptimizeVisitorManager<Instruction>()

    init {
        counterVisitorManager.register(MethodInvocation::class.java, MethodInvocationVisitor)
        counterVisitorManager.register(VariableAccess::class.java, VariableAccessVisitor)

        // Optimizers
        optimizeVisitorManager.register(MethodInvocation::class.java, MethodInvocationOptimizeVisitor)
        optimizeVisitorManager.registerSuper(VariableAccess::class.java, VariableAccessOptimizeVisitor)
    }

    override fun inline(
        args: List<Instruction>,
        declaration: MethodDeclaration
    ): List<Instruction> {
        val insns = mutableListOf<Instruction>()
        insns.addAll(declaration.parameters.mapIndexed { index, it ->
            variable(type = it.type, name = it.name, args[index])
        })
        insns.addAll(declaration.body)
        return optimize(insns)
    }

    /**
     * Simple optimization step to remove auxiliary instructions if the inline code only use it only one time.
     */
    private fun optimize(insns: List<Instruction>): List<Instruction> {
        val vars = mutableMapOf<String, Instruction>()
        for (insn in insns) {
            if (insn is VariableDeclaration && insn.name.startsWith("__p")) {
                vars[insn.name] = insn.value
            }
        }
        val data = TypedData()

        for (insn in insns) {
            counterVisitorManager.visit(insn, data)
        }

        val usageCounter = VARIABLE_USAGE_COUNTER.getOrNull(data)

        if (usageCounter == null) {
            return insns
        } else {
            val optimizeOutVars = mutableMapOf<String, Instruction>()
            for ((v, insn) in vars) {
                val counter = usageCounter[v]
                if (counter != null && counter == 1L) {
                    optimizeOutVars[v] = insn
                }
            }

            if (optimizeOutVars.isNotEmpty()) {
                val optimizeData = TypedData()
                OPTIMIZE_VARS.set(optimizeData, optimizeOutVars)

                val newInsns = insns.filter {
                    it !is VariableDeclaration || !optimizeOutVars.containsKey(it.name)
                }.map {
                    optimizeVisitorManager.visit(it, optimizeData)
                }

                return newInsns
            }
        }

        return insns
    }
}