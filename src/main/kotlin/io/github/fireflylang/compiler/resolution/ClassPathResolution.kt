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
package io.github.fireflylang.compiler.resolution

import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.common.MethodSpec
import com.github.jonathanxd.kores.util.conversion.methodDeclarations
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap

class ClassPathResolution {
    private val cache = ConcurrentHashMap<String, List<MethodDeclaration>>()

    fun lookupFor(
        location: String,
        signature: MethodSpec
    ): MethodDeclaration? {
        // TODO: Resolve by assignable types
        return cache.computeIfAbsent(location) {
            val loadedClass = Class.forName(location)
            loadedClass.methodDeclarations
        }.firstOrNull { it.match(signature) }
    }
}