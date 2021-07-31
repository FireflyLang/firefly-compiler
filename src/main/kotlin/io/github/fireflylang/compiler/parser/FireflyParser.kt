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

import io.github.fireflylang.compiler.FireflyCompiledUnit
import io.github.fireflylang.compiler.FireflyDeclaredUnit
import io.github.fireflylang.compiler.FireflyUnit
import io.github.fireflylang.compiler.errors.Error
import io.github.fireflylang.compiler.errors.ErrorReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.*

class FireflyParser(
    val receiveChannel: ReceiveChannel<FireflyUnit>,
    val publishChannel: SendChannel<FireflyDeclaredUnit>,
    val errorReport: ErrorReport
) {
    private val logger = KotlinLogging.logger("FireflyParser")

    fun runParser(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val jobs = LinkedList<Job>()
            while (true) {
                try {
                    val toParse = receiveChannel.receive()
                    val parseJob = CoroutineScope(Dispatchers.IO).launch {
                        parse(toParse, publishChannel, errorReport)
                    }
                    jobs.add(parseJob)
                } catch (e: ClosedReceiveChannelException) {
                    break;
                } catch (e: Exception) {
                    logger.error(e) {
                        "Failed to parse unit"
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
}