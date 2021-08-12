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
package io.github.fireflylang.compiler

import com.github.jonathanxd.iutils.`object`.TypedKey
import com.github.jonathanxd.iutils.kt.typeInfo
import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.MutableInstructions
import com.github.jonathanxd.kores.base.KoresParameter
import com.github.jonathanxd.kores.base.MethodDeclaration
import io.github.fireflylang.compiler.ast.AstParameterDefault

val ENTRYPOINT_METHOD = TypedKey("ENTRYPOINT_METHOD", typeInfo<MethodDeclaration>())
val CURRENT_METHOD = TypedKey("CURRENT_METHOD", typeInfo<MethodDeclaration>())
val CURRENT_SOURCE = TypedKey("CURRENT_SOURCE", typeInfo<MutableInstructions>())
val ENTRYPOINT_METHOD_SOURCE = TypedKey("ENTRYPOINT_METHOD_SOURCE", typeInfo<MutableInstructions>())
val ARGUMENTS = TypedKey("ARGUMENTS", typeInfo<MutableList<Instruction>>())
val PARAMETERS = TypedKey("PARAMETERS", typeInfo<MutableList<KoresParameter>>())
val CURRENT_PARAMETER = TypedKey("CURRENT_PARAMETER", typeInfo<KoresParameter>())
val PARAMETERS_DEFAULTS = TypedKey("PARAMETERS_DEFAULTS", typeInfo<MutableList<AstParameterDefault>>())
val CURRENT_INSTRUCTIONS = TypedKey("CURRENT_INSTRUCTIONS", typeInfo<MutableList<Instruction>>())