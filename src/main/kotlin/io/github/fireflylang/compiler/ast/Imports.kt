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

data class Imports(
    val list: List<Import>
) {
    constructor(vararg imports: Import): this(imports.toList())
}

/**
 * Represents import of units, namespaces or packages.
 *
 * Firefly supports different ways of importing things, for example:
 *
 * ```firefly
 * import io.github.fireflylang.Std[String, Int] // Imports 'String' and 'Int' types from Std namespace
 * import io.github.fireflylang.println // Imports the 'println' function from 'io.github.fireflylang' package
 * import io.github.fireflylang.String // Imports the 'String' type from 'io.github.fireflylang' package
 * ```
 *
 * Because of that, it is impossible to declare more than one type or function with the same name within the same package.
 * Firefly will always look into all namespaces for a function, variable, type or namespace.
 *
 * However, it is possible to declare two sub-namespaces with the same name, for example:
 *
 * `io/github/fireflylang/Std.firefly
 * ```firefly
 * namespace Primitives {
 *     type Int
 *     type Long
 *     type Boolean
 *     type Decimal
 *     type Unit
 *     type Character
 * }
 *
 * type String
 * ```
 *
 * To import things in sub-namespaces it is required to provide the parent namespace name, for example:
 *
 * ```firefly
 * import io.github.fireflylang.Std.Primitives[Int] // Valid!
 * import io.github.fireflylang.Primitives[Int] // Invalid!!!
 * import io.github.fireflylang.Int // Invalid!!!
 * ```
 */
data class Import(
    val path: String,
    val units: List<String>,
    val alias: String? = null,
    val type: ImportType
)

enum class ImportType {
    /**
     * Namespace is defined by a file, for example, the file `io/github/fireflylang/Std.firefly`
     * represents the `Std` namespace.
     */
    NAMESPACE,

    /**
     * Represents a unit located at a namespace, a namespace can have multiple units defined.
     *
     * Units are:
     * - Types
     * - Classes
     * - Interfaces
     * - Rules
     * - Traits
     * - Enums
     * - Sub-namespaces
     */
    UNIT,

    /**
     * Represents a directory, for example `io/github/fireflylang`.
     *
     * Firefly does not have the `package` keyword, instead, the package is inferred from the directory relative to
     * `sourceDir`.
     */
    PACKAGE
}