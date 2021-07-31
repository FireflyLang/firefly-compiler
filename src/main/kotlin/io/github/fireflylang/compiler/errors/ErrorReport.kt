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
package io.github.fireflylang.compiler.errors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import mu.KotlinLogging

class ErrorReport() {

    val channel = Channel<Error>()
    private val logger = KotlinLogging.logger("ErrorReport")

    fun runErrorReport(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val error = channel.receive()
                    report(error)
                } catch (t: ClosedReceiveChannelException) {
                    break;
                } catch (e: Throwable) {
                    throw IllegalStateException(e)
                }
            }
        }
    }

    private fun report(e: Error) {
        when (e) {
            is ParsingExceptionError -> {
                logger.error(e.exception) {
                    "An exception occurred while parsing Firefly Source Units"
                }
            }
            is CompilationExceptionError -> {
                logger.error(e.exception) {
                    "An exception occurred while compiling Firefly Source Units"
                }
            }
            is UnitCompilationError -> {
                val line = e.ctx.start.line
                val stopLine = e.ctx.stop.line
                val startIndex = e.ctx.start.startIndex
                val endIndex = e.ctx.start.stopIndex
                val stopStartIndex = e.ctx.stop.startIndex
                val stopEndIndex = e.ctx.stop.stopIndex
                logger.error {
                    "An error occurred while compiling Firefly Source Unit: ${e.buildPath()}\n" +
                            "  Message: ${e.message}.\n" +
                            "  Additional details: ${e.details.joinToString(separator = "\n  ")}"
                }
            }
        }
    }

    private fun UnitCompilationError.buildPath(): String {
        val pathString = this.unit.path.toString()
        val sb = StringBuilder()
        if (pathString.isEmpty() || pathString.isBlank()) {
            sb.append(this.unit.fileName)
        }
        val line = this.ctx.start.line

        if (this.ctx.start == this.ctx.stop) {
            val stopLine = this.ctx.stop.line
            val startIndex = this.ctx.start.startIndex
            val endIndex = this.ctx.start.stopIndex
            if (stopLine != line) {
                sb.append(":").append(line).append(":").append(stopLine).append(" ")
            } else {
                sb.append(":").append(line).append(" ")
            }
            sb.append(startIndex).append(":").append(endIndex)
        } else {
            val stopLine = this.ctx.stop.line
            val startStartIndex = this.ctx.start.startIndex
            //val endIndex = this.ctx.start.stopIndex
            //val stopStartIndex = this.ctx.stop.startIndex
            val stopEndIndex = this.ctx.stop.stopIndex
            if (stopLine != line) {
                sb.append(":").append(line).append(":").append(stopLine).append(" ")
            } else {
                sb.append(":").append(line).append(" ")
            }
            sb.append(startStartIndex).append("::").append(stopEndIndex)
        }
        return sb.toString()
    }

}