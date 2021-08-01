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
package io.github.fireflylang.compiler.test.resolution

import com.github.jonathanxd.kores.Types
import com.github.jonathanxd.kores.base.KoresModifier
import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.common.MethodSpec
import com.github.jonathanxd.kores.dsl.parameter
import com.github.jonathanxd.kores.factory.typeSpec
import io.github.fireflylang.compiler.FireflyUnit
import io.github.fireflylang.compiler.UnitType
import io.github.fireflylang.compiler.resolution.ResolutionTable
import io.github.fireflylang.compiler.resolution.ResolvedMethodDeclaration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.antlr.v4.runtime.CharStreams
import java.nio.file.Paths

class ResolutionTableTest : FunSpec({
    test("store resolved method declaration") {
        val table = ResolutionTable()

        val type = "io.github.fireflylang.Printer"
        val unit = FireflyUnit(
            fileName = "Printer.firefly",
            path = Paths.get("io", "github", "fireflylang"),
            contentStream = {
                CharStreams.fromString("fun println(message: String) { System.out.println(message) }")
            },
            UnitType.CLASS
        )

        val methodDeclaration = MethodDeclaration.Builder.builder()
            .modifiers(KoresModifier.PUBLIC)
            .name("println")
            .returnType(Types.VOID)
            .parameters(parameter(type = Types.STRING, name = "message"))
            .build()

        val complete = CompletableDeferred<MethodDeclaration?>()
        val job = CoroutineScope(Dispatchers.IO).launch {
            complete.complete(table.lookupFor(
                type,
                MethodSpec("println", typeSpec(Types.VOID, Types.STRING))
            ))
        }

        // Wait until there is at least one subscriber
        val atLeastOne = table.subscribers().first { it > 0 }

        val register = async(Dispatchers.IO) {
            val r = table.registerResolvedMethodDeclaration(ResolvedMethodDeclaration(type, unit, methodDeclaration))
            table.endResolution()
            r
        }

        register.await()

        val found = complete.await()
        found shouldBe methodDeclaration
    }
})