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
package io.github.fireflylang.compiler.global

import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.Instructions
import com.github.jonathanxd.kores.Types
import com.github.jonathanxd.kores.base.*
import com.github.jonathanxd.kores.common.MethodTypeSpec
import com.github.jonathanxd.kores.dsl.parameter
import com.github.jonathanxd.kores.factory.accessVariable
import com.github.jonathanxd.kores.type.typeOf
import io.github.fireflylang.compiler.ast.accessSystemErr
import io.github.fireflylang.compiler.ast.accessSystemOut
import java.io.PrintStream
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicLong

object GlobalInlineFunctions {

    private val printlnCounter = AtomicLong()

    private fun printlnInMethod(
        acc: Instruction,
        name: String,
        types: List<Type>
    ) = printlnCounter.incrementAndGet().let { synth ->
        val parameterNameResolver = { i: Int -> "__p$$synth$$i" }
        MethodDeclaration.Builder.builder()
            .modifiers(KoresModifier.PUBLIC, KoresModifier.SYNTHETIC)
            .name("println")
            .returnType(Types.VOID)
            .parameters(types.mapIndexed { index, type -> parameter(type = type, name = parameterNameResolver(index)) })
            .body(Instructions.fromPart(
                MethodInvocation.Builder.builder()
                    .invokeType(InvokeType.INVOKE_VIRTUAL)
                    .localization(typeOf<PrintStream>())
                    .target(acc)
                    .spec(MethodTypeSpec(
                        typeOf<PrintStream>(),
                        name,
                        TypeSpec(Types.VOID, types)
                    ))
                    .arguments(types.mapIndexed { index, type -> accessVariable(type = type, name = parameterNameResolver(index)) })
                    .build()
            ))
            .build()
    }


    fun isGlobal(name: String) = when (name) {
        "println", "eprintln", "print", "eprint" -> true
        else -> false
    }

    /**
     * Resolves a global function which must be inlined.
     */
    fun resolveInlinedFunction(
        name: String,
        types: List<Type>
    ): MethodDeclaration? {
        return when(name) {
            "println" -> printlnInMethod(accessSystemOut(), "println", types)
            "eprintln" -> printlnInMethod(accessSystemErr(), "println", types)
            "print" -> printlnInMethod(accessSystemOut(), "print", types)
            "eprint" -> printlnInMethod(accessSystemErr(), "print", types)
            else -> null
        }
    }


}