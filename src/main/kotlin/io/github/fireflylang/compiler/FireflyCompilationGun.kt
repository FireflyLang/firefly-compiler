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

import io.github.fireflylang.compiler.errors.Error
import io.github.fireflylang.compiler.errors.ErrorReport
import io.github.fireflylang.compiler.parser.FireflyParser
import io.github.fireflylang.compiler.parser.parse
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import java.util.*

class FireflyCompilationGun {
    private val logger = KotlinLogging.logger("FireflyCompilationGun")

    fun compile(units: Channel<FireflyUnit>): Channel<FireflyCompiledUnit> {
        val unitChannel = Channel<FireflyDeclaredUnit>()
        val compilationResultChannel = Channel<FireflyCompiledUnit>()
        val errorReport = ErrorReport()
        val parser = FireflyParser(units, unitChannel, errorReport)
        val compiler = FireflyCompiler(unitChannel, compilationResultChannel)
        val parserJob = parser.runParser()
        val compilationJob = compiler.runCompilation()
        val errorReportJob = errorReport.runErrorReport()

        CoroutineScope(Dispatchers.IO).launch {
            parserJob.join()
            compilationJob.join()
            errorReport.channel.close()
            errorReportJob.join()
        }
        return compilationResultChannel
    }

    suspend fun compileSingleUnit(unit: FireflyUnit): FireflyCompiledUnit {
        val channel = Channel<FireflyUnit>()
        val receive = this.compile(channel)
        channel.send(unit)

        val recv = receive.receive()
        channel.close()
        return recv
    }


    /*fun compile(units: Channel<FireflyUnit>): Channel<FireflyCompiledUnit> {
        val unitChannel = Channel<FireflyDeclaredUnit>()
        val compilationResultChannel = Channel<FireflyCompiledUnit>()
        val compiler = FireflyCompiler(unitChannel, compilationResultChannel)
        compiler.runCompilation()

        val closeSignal = Channel<Boolean>()
        CoroutineScope(Dispatchers.IO).launch {
            var signals = 0
            while (signals < 8) {
                delay(100L)
                if (closeSignal.receive()) {
                    ++signals
                }
            }

            unitChannel.close()
        }


        for (i in 0..8) {
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    try {
                        val toParse = units.receive()
                        parse(toParse, unitChannel)
                    } catch (e: ClosedReceiveChannelException) {
                        closeSignal.send(true)
                        break;
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Failed to parse unit"
                        }
                        break;
                    }
                }
                *//*try {
                unitChannel.close()
                units.close()
            } catch (e: Exception) {
            }*//*
            }
        }

        return compilationResultChannel
    }

    suspend fun compileSingleUnit(unit: FireflyUnit): FireflyCompiledUnit {
        val channel = Channel<FireflyUnit>()
        val receive = this.compile(channel)
        channel.send(unit)

        val recv = receive.receive()
        channel.close()
        return recv
    }
    */
}

