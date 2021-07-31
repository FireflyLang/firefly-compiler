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
package io.github.fireflylang.compiler.ast

import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.Types
import com.github.jonathanxd.kores.base.FieldAccess
import com.github.jonathanxd.kores.base.InvokeType
import com.github.jonathanxd.kores.base.MethodInvocation
import com.github.jonathanxd.kores.base.TypeSpec
import com.github.jonathanxd.kores.common.MethodTypeSpec
import com.github.jonathanxd.kores.literal.Literals
import com.github.jonathanxd.kores.type.typeOf
import java.io.PrintStream
import java.lang.reflect.Type

fun invokePrintln2(
    inferredTypes: List<Type> = emptyList(),
    args: List<Instruction> = emptyList()
): MethodInvocation =
    MethodInvocation.Builder.builder()
        .invokeType(InvokeType.INVOKE_VIRTUAL)
        .localization(typeOf<PrintStream>())
        .target(accessSystemOut())
        .spec(MethodTypeSpec(
            typeOf<PrintStream>(),
            "println",
            TypeSpec(Types.VOID, inferredTypes))
        )
        .arguments(args)
        .build()

fun invokePrintln(
    inferredTypes: List<Type> = emptyList(),
    args: List<Instruction> = emptyList()
): MethodInvocation =
    MethodInvocation.Builder.builder()
        .invokeType(InvokeType.INVOKE_VIRTUAL)
        .localization(typeOf<PrintStream>())
        .target(accessSystemOut())
        .spec(MethodTypeSpec(
            typeOf<PrintStream>(),
            "println",
            TypeSpec(Types.VOID, listOf(typeOf<String>())))
        )
        .arguments(listOf(Literals.STRING("Hello From Firefly")))
        .build()

