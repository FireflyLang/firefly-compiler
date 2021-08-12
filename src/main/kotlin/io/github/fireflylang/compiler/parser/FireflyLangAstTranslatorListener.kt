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
import com.github.jonathanxd.iutils.kt.set
import com.github.jonathanxd.kores.MutableInstructions
import com.github.jonathanxd.kores.Types
import com.github.jonathanxd.kores.base.KoresModifier
import com.github.jonathanxd.kores.base.KoresParameter
import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.type
import com.github.jonathanxd.kores.type.typeOf
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
import io.github.fireflylang.compiler.global.GlobalInlineFunctions
import io.github.fireflylang.compiler.inliner.StandardFunctionInliner
import io.github.fireflylang.compiler.resolution.ResolutionTables
import io.github.fireflylang.compiler.resolution.TypeSignature
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

class FireflyLangAstTranslatorListener(
    val unit: FireflyUnit,
    val ctx: ParseContext,
    val publishChannel: SendChannel<FireflyDeclaredUnit>
) : FireflyLangBaseListener() {
    private val logger = KotlinLogging.logger("FireflyLangListener")
    private val inliner = StandardFunctionInliner()
    private val imports = AtomicReference(Imports())
    private val resolutionTables get() = this.ctx.resolutionTables
    private val errorReport get() = this.ctx.errorReport

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

    override fun enterImports(ctx: FireflyLangParser.ImportsContext) {
        val imports = ctx.importStm().map {
            val importId = it.ID().toString()
            val alias = it.importAlias()?.ID()
            val sub = it.namespaceSubImport()?.subImports()?.ID().orEmpty()
            val importType = if (sub.isNotEmpty()) ImportType.NAMESPACE else ImportType.UNIT
            Import(importId, sub.map { it.toString() }, alias?.toString(), importType)
        }

        this.imports.set(Imports(imports))
    }

    override fun enterFnDeclaration(ctx: FireflyLangParser.FnDeclarationContext) {
        // TODO: Local methods
        val methods = classDec.methods as MutableList<MethodDeclaration>
        val name = ctx.identifier().ID().toString()

        val parameters = ctx.parameters()?.toParameters().orEmpty()  // TODO: Parameters
        val returnType = Types.VOID // TODO: Return type
        val modifiers = mutableSetOf(KoresModifier.PUBLIC)
        methods.add(
            MethodDeclaration.Builder.builder()
                .modifiers(modifiers)
                .name(name)
                .returnType(returnType)
                .parameters(parameters)
                .body(MutableInstructions.create())
                .build()
                .also {
                    val newData = pushData()
                    newData[CURRENT_SOURCE] = it.body as MutableInstructions
                }
        )
    }

    fun FireflyLangParser.ParametersContext.toParameters(): List<KoresParameter> =
        this.parameter().map {
            val name = it.ID().toString()
            val typeName = it.type()?.ID()?.toString()
            val type =
                if (typeName == null) AstDynamicType
                else runBlocking {
                    resolutionTables.type.lookupFor(typeName, TypeSignature(typeName)).also { resolved ->
                        if (resolved == null)
                            reportError(UnitCompilationError(
                                unit,
                                it,
                                "Could not resolve the type '$typeName'",
                                listOf("The type could not be found during the resolution step.")
                            ))
                    }
                } ?: AstDynamicType

            KoresParameter.Builder.builder()
                .modifiers(KoresModifier.FINAL)
                .name(name)
                .type(type)
                .build()
        }

    override fun exitFnDeclaration(ctx: FireflyLangParser.FnDeclarationContext) {
        popData()
    }

    override fun enterInvokeFunc(ctx: FireflyLangParser.InvokeFuncContext) {
        val id = ctx.identifier().ID().toString()
        val inline = GlobalInlineFunctions.isGlobal(id)

        if (inline) {
            val next = pushData()
            CURRENT_INSTRUCTIONS.set(next, mutableListOf())
        }

        logger.info { "Entered invocation id: $id" }
    }

    override fun exitInvokeFunc(ctx: FireflyLangParser.InvokeFuncContext) {
        val id = ctx.identifier().ID().toString()
        val inline = GlobalInlineFunctions.isGlobal(id)
        if (inline) {
            val prev = popData()
            val args = CURRENT_INSTRUCTIONS.require(prev)
            if (CURRENT_SOURCE.contains(this.currentData())) {
                val source = CURRENT_SOURCE.require(this.currentData())
                val inlinedFunction = GlobalInlineFunctions.resolveInlinedFunction(
                    id,
                    args.map { it.type }
                )!!
                val inlinedInstructions = inliner.inline(args, inlinedFunction)
                source.addAll(inlinedInstructions)
            } else {
                val inlinedFunction = GlobalInlineFunctions.resolveInlinedFunction(
                    id,
                    args.map { it.type }
                )!!
                val inlinedInstructions = inliner.inline(args, inlinedFunction)

                CURRENT_INSTRUCTIONS.require(prev).addAll(inlinedInstructions)
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
        runBlocking {
            publishChannel.send(FireflyDeclaredUnit(unit, classDec))
        }
    }
}