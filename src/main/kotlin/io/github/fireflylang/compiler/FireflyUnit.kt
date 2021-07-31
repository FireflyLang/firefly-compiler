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

import org.antlr.v4.runtime.CharStream
import java.nio.file.Path

class FireflyUnit(
    /**
     * The file name of this compilation unit, with the `.firefly` extension.
     */
    val fileName: String,
    /**
     * The path of this compilation unit, relative to `src/firefly/`
     */
    val path: Path,
    /**
     * The content stream
     */
    val contentStream: () -> CharStream,
    /**
     * The type of this unit
     */
    val unitType: UnitType
) {
    val unitName = this.fileName.substring(0, this.fileName.lastIndexOf('.'))
    val qualifiedName = this.path.toString().let { p ->
        if (p.isEmpty() || p.isBlank()) {
            this.unitName
        } else {
            "${p.replace("/", ".")}.${this.unitName}"
        }
    }
}

enum class UnitType {
    OBJECT,
    CLASS,
    INTERFACE,
    RULE,
    UNIT
}