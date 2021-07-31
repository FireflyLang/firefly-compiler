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

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.kores.base.TypeDeclaration
import com.github.jonathanxd.kores.bytecode.BytecodeClass
import com.github.jonathanxd.kores.bytecode.processor.BytecodeGenerator
import com.github.jonathanxd.kores.bytecode.processor.SOURCE_FILE_FUNCTION
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import java.util.*

private val generator = BytecodeGenerator()

class FireflyCompiler(
    val receiveChannel: ReceiveChannel<FireflyDeclaredUnit>,
    val publishChannel: SendChannel<FireflyCompiledUnit>
) {
    private val logger = KotlinLogging.logger("FireflyCompiler")

    fun runCompilation(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val jobs = LinkedList<Job>()
            while (true) {
                try {
                    val unit = receiveChannel.receive()
                    jobs.add(CoroutineScope(Dispatchers.IO).launch {
                        val data = generator.createData()

                        SOURCE_FILE_FUNCTION.set(data) {
                            unit.unit.fileName
                        }

                        val result = generator.process(unit.typeDeclaration, data)

                        publishChannel.send(
                            FireflyCompiledUnit(
                                unit,
                                result
                            )
                        )
                    })
                } catch (e: ClosedReceiveChannelException) {
                    break;
                } catch (e: Exception) {
                    logger.error(e) {
                        "Compilation failed"
                    }
                    break;
                }
            }

            for (job in jobs) {
                job.join()
            }

            publishChannel.close()
        }
    }

    /*
    fun runCompilation() {
        val closeSignal = Channel<Boolean>()
        CoroutineScope(Dispatchers.IO).launch {
            var signals = 0
            while (signals < 8) {
                delay(100L)
                if (closeSignal.receive()) {
                    ++signals
                }
            }

            publishChannel.close()
        }

        for(i in 0..8) {
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    try {
                        val unit = receiveChannel.receive()
                        val data = generator.createData()

                        SOURCE_FILE_FUNCTION.set(data) {
                            unit.unit.fileName
                        }

                        val result = generator.process(unit.typeDeclaration, data)

                        publishChannel.send(
                            FireflyCompiledUnit(
                                unit,
                                result
                            )
                        )
                    } catch (e: ClosedReceiveChannelException) {
                        closeSignal.send(true)
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Compilation failed"
                        }
                        break;
                    }
                }
            }
        }
    }
     */
}

fun Flow<FireflyDeclaredUnit>.compileAll(): Flow<List<BytecodeClass>> {
    return this.map { unit ->
        val data = TypedData(generator.createData())

        SOURCE_FILE_FUNCTION.set(data) {
            unit.unit.fileName
        }

        generator.process(unit.typeDeclaration, data)
    }
}