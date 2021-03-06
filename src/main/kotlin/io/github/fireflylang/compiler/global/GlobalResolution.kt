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

import com.github.jonathanxd.kores.base.MethodDeclaration
import io.github.fireflylang.compiler.ast.Import
import io.github.fireflylang.compiler.ast.ImportType
import io.github.fireflylang.compiler.ast.Imports

/**
 * Resolve global symbols, such as `print`, `eprint`, `println`, `eprintln`, `read`, `readln`, and other
 * globally available functions, variables and types.
 */
object GlobalResolution {

    private val globalImports = Imports(
        Import(
            path = "io.github.fireflylang.Std.Primitives",
            units = listOf("Character", "Double", "Byte", "Char", "Int", "Decimal", "Unit"),
            type = ImportType.NAMESPACE
        ),
        Import(
            path = "io.github.fireflylang.Std",
            units = listOf("String", "print", "eprint", "println", "eprintln", "read", "readln"),
            type = ImportType.NAMESPACE
        )
    )



    /**
     *
     */
    class Inline {

    }
}