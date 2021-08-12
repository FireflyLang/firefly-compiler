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
package io.github.fireflylang.compiler.test.parse

import com.github.jonathanxd.kores.bytecode.classloader.CodeClassLoader
import com.github.jonathanxd.kores.bytecode.util.save
import io.github.fireflylang.compiler.FireflyCompilationGun
import io.github.fireflylang.compiler.FireflyCompiledUnit
import io.github.fireflylang.compiler.FireflyUnit
import io.github.fireflylang.compiler.errors.ErrorReport
import io.github.fireflylang.compiler.parser.parse
import io.github.fireflylang.compiler.test.snippets.*
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

class FireflyFnTest : FunSpec({
    test("simple fn invocation") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simpleFn)
        val end = Instant.now()
        save(result)
        val loader = CodeClassLoader()
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())

        val ctr = clazz.getDeclaredConstructor()
        ctr.isAccessible = true
        val ins = ctr.newInstance()
        val hello = clazz.getDeclaredMethod("hello")
        hello.invoke(ins, *emptyArray())
        println("Compilation took ${Duration.between(start, end)}")
    }

    test("simple fn with default param invocation") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simpleFnWithDefault)
        val end = Instant.now()
        save(result)
        val loader = CodeClassLoader()
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())

        val ctr = clazz.getDeclaredConstructor()
        ctr.isAccessible = true
        val ins = ctr.newInstance()
        val hello = clazz.getDeclaredMethod("hello")
        hello.invoke(ins, *emptyArray())
        println("Compilation took ${Duration.between(start, end)}")
    }
})

fun save(result: FireflyCompiledUnit) {
    val generatedClasses = Paths.get("generated-classes")
    Files.createDirectories(generatedClasses)
    result.compilationResult.forEach {
        it.save(
            generatedClasses,
            true,
            true
        )
    }
}