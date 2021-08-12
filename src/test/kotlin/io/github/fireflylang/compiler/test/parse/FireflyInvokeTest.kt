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
import io.github.fireflylang.compiler.parser.ParseContext
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

class FireflyInvokeTest : FunSpec({
    test("simple invocation") {
        parse(simpleInvocation, Channel(10), ParseContext())
    }
    test("simple plus invocation") {
        parse(simplePlusInvocation, Channel(10), ParseContext())
    }
    test("simple println invocation") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simplePrintlnInvocation)
        val end = Instant.now()
        val loader = CodeClassLoader()
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())
        println("Compilation took ${Duration.between(start, end)}")
    }

    test("simple println hello world invocation") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simplePrintlnHelloWorldInvocation)
        val end = Instant.now()
        val loader = CodeClassLoader()
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())
        println("Compilation took ${Duration.between(start, end)}")
    }

    test("simple println hello world error invocation") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simpleEPrintlnHelloWorldInvocation)
        val end = Instant.now()
        val loader = CodeClassLoader()
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())
        println("Compilation took ${Duration.between(start, end)}")
    }

    test("simple println hello world error 2x invocation") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simpleEPrintlnHelloWorld2xInvocation)
        val end = Instant.now()
        val loader = CodeClassLoader()
        val generatedClasses = Paths.get("generated-classes")
        Files.createDirectories(generatedClasses)
        result.compilationResult.forEach {
            it.save(
                generatedClasses,
                true,
                true
            )
        }
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())
        println("Compilation took ${Duration.between(start, end)}")
    }

    test("simple null argument") {
        val compile = FireflyCompilationGun()
        val start = Instant.now()
        val result = compile.compileSingleUnit(simpleNullArgInvocation)
        val end = Instant.now()
        val loader = CodeClassLoader()
        val clazz = loader.define(result.compilationResult)
        println(clazz)
        println(clazz.declaredMethods.contentToString())
        val ep = clazz.getDeclaredMethod("entrypoint", Array<String>::class.java)
        ep.invoke(null, arrayOf<String>())
        println("Compilation took ${Duration.between(start, end)}")
    }

    test("simple println invocation timings") {
        val compile = FireflyCompilationGun()
        val samplesTotal = 10000
        val samples = ArrayList<Duration>(samplesTotal)
        val results = ArrayList<FireflyCompiledUnit>(samplesTotal)
        for (i in 0..samplesTotal) {
            val start = Instant.now()
            val result = compile.compileSingleUnit(simplePrintlnInvocation)
            val end = Instant.now()
            results.add(result)
            samples.add(Duration.between(start, end))
        }
        val max = samples.maxOf { it }
        val min = samples.minOf { it }
        val sum = samples.reduce { acc, duration -> acc.plus(duration) }
        val nineth = Duration.ofNanos((sum.toNanos().toDouble() * (99.0 / 100.0)).toLong());
        val nfiveth = Duration.ofNanos((sum.toNanos().toDouble() * (95.0 / 100.0)).toLong());
        val fiveth = Duration.ofNanos((sum.toNanos().toDouble() * (50.0 / 100.0)).toLong());
        val avg = sum.dividedBy(samplesTotal.toLong())

        println("Compilations took avg of $avg. Max: $max. Min: $min. 99th: $nineth. 95th: $nfiveth. 50th: $fiveth.")
    }

    test("simple println invocation timings gunned") {
        val compile = FireflyCompilationGun()
        val samplesTotal = 10000
        val samples = ArrayList<Duration>(samplesTotal)
        val results = ArrayList<FireflyCompiledUnit>(samplesTotal)
        val jobs = ArrayList<Job>(samplesTotal)

        val units = Channel<FireflyUnit>()
        for (i in 0..samplesTotal) {
            jobs.add(CoroutineScope(Dispatchers.IO).launch {
                units.send(simplePrintlnInvocation)
            })
        }

        // Channel closer
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                val allCompleted = jobs.all { it.isCompleted }
                if (!allCompleted)
                    continue
                else {
                    try {
                        units.close()
                    } catch (e: Throwable) {

                    }
                    break;
                }
            }
        }

        val resultChannel = compile.compile(units)
        while (true) {
            try {
                val start = Instant.now()
                val recv = resultChannel.receive()
                val end = Instant.now()
                results.add(recv)
                samples.add(Duration.between(start, end))
            } catch(e: ClosedReceiveChannelException) {
                break;
            } catch(e: Throwable) {
                e.printStackTrace()
                break;
            }
        }

        val max = samples.maxOf { it }
        val min = samples.minOf { it }
        val sum = samples.reduce { acc, duration -> acc.plus(duration) }
        val nineth = Duration.ofNanos((sum.toNanos().toDouble() * (99.0 / 100.0)).toLong());
        val nfiveth = Duration.ofNanos((sum.toNanos().toDouble() * (95.0 / 100.0)).toLong());
        val fiveth = Duration.ofNanos((sum.toNanos().toDouble() * (50.0 / 100.0)).toLong());
        val avg = sum.dividedBy(samplesTotal.toLong())

        println("Compilations took avg of $avg. Max: $max. Min: $min. 99th: $nineth. 95th: $nfiveth. 50th: $fiveth.")
    }
})