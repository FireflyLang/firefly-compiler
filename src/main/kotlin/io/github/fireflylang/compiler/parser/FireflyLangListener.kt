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
package io.github.fireflylang.compiler.parser

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.iutils.kt.require
import com.github.jonathanxd.kores.type
import io.github.fireflylang.compiler.*
import io.github.fireflylang.compiler.ast.*
import io.github.fireflylang.compiler.errors.UnitCompilationError
import io.github.fireflylang.compiler.grammar.FireflyLangBaseListener
import io.github.fireflylang.compiler.grammar.FireflyLangParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*
import io.github.fireflylang.compiler.errors.Error
import io.github.fireflylang.compiler.errors.ErrorReport

class FireflyLangListener(
    val unit: FireflyUnit,
    val publishChannel: SendChannel<FireflyDeclaredUnit>,
    val errorReport: ErrorReport
) : FireflyLangBaseListener() {
    private val logger = KotlinLogging.logger("FireflyLangListener")

    private val stack = Stack<TypedData>().also {
        it.push(TypedData())
    }

    fun currentData() = stack.peek()!!

    private val classDec = createTypeDeclaration(unit, this.currentData())

    fun pushData(): TypedData {
        val next = TypedData(this.currentData())
        this.stack.push(next)
        return next
    }

    fun popData(): TypedData {
        return stack.pop()
    }

    override fun enterInvokeFunc(ctx: FireflyLangParser.InvokeFuncContext) {
        val id = ctx.identifier().ID().toString()
        if (id == "println") {
            val source = CURRENT_SOURCE.require(this.currentData())
            val next = pushData()
            CURRENT_INSTRUCTIONS.set(next, mutableListOf())
        }
        logger.info { "Entered invocation id: $id" }
    }

    override fun exitInvokeFunc(ctx: FireflyLangParser.InvokeFuncContext) {
        val id = ctx.identifier().ID().toString()
        if (id == "println") {
            val prev = popData()
            val args = CURRENT_INSTRUCTIONS.require(prev)
            if (CURRENT_SOURCE.contains(this.currentData())) {
                val source = CURRENT_SOURCE.require(this.currentData())

                source.add(
                    invokePrintln2(
                        args.map { it.type },
                        args
                    )
                )
            } else {
                CURRENT_INSTRUCTIONS.require(prev).add(
                    invokePrintln2(
                        args.map { it.type },
                        args
                    )
                )
            }
        }
        logger.info { "Exited invocation id: $id" }
    }

    override fun enterLiteral(ctx: FireflyLangParser.LiteralContext) {
        val insns = CURRENT_INSTRUCTIONS.require(this.currentData())
        val stringLiteral = ctx.StringLiteral()
        val booleanLiteral = ctx.BooleanLiteral()
        val characterLiteral = ctx.CharacterLiteral()
        val integerLiteral = ctx.IntegerLiteral()
        val floatingPointLiteral = ctx.FloatingPointLiteral()
        val nullLiteral = ctx.NullLiteral();

        when {
            stringLiteral != null -> insns.add(stringLiteral.toStringLiteral())
            booleanLiteral != null -> insns.add(booleanLiteral.toBooleanLiteral())
            integerLiteral != null -> insns.add(integerLiteral.toIntLiteral())
            floatingPointLiteral != null -> insns.add(floatingPointLiteral.toDecimalLiteral())
            nullLiteral != null ->
                this.reportError(UnitCompilationError(
                    unit,
                    ctx,
                    "Null literal is not allowed",
                    listOf("Firefly does not support 'null' elements.")
                ))
            else -> throw UnsupportedOperationException("Unsupported Literal: $ctx!")
        }
    }

    private fun reportError(ctx: Error) {
        CoroutineScope(Dispatchers.IO).launch {
            errorReport.channel.send(ctx)
        }
    }

    override fun exitUnit(ctx: FireflyLangParser.UnitContext?) {
        CoroutineScope(Dispatchers.IO).launch {
            publishChannel.send(FireflyDeclaredUnit(unit, classDec))
        }
    }
}