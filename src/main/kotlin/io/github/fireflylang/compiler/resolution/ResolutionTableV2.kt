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
import com.github.jonathanxd.kores.base.Named
import com.github.jonathanxd.kores.common.MethodSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import mu.KotlinLogging

/**
 * A resolution table which stores [MethodDeclarations][MethodDeclaration] by class name and function name,
 * providing fast access to resolved methods.
 *
 * Access to resolved functions are done through Actor messaging, the function responsible to communicate
 * with the actor which fetches and stores data is the [lookupFor] function. Storing resolved [MethodDeclaration]
 * is done through [registerResolvedMethodDeclaration].
 *
 * Actor is used to ensure that only one Coroutine accesses the resolution table at a time.
 *
 * **The most common way** of doing resolutions is through [ResolutionMedium], which first tries to resolve
 * based on classpath, and then it calls [ResolutionTable] functions to resolve the method.
 *
 * **Note:** Implementation of a more efficient locking mechanism is possible by accounting all
 * types at a time and storing the inner [MutableMap] which contains resolved methods, and then
 * doing a lock **unit-wise** (**class-wise**) instead of **compilation-wise**.
 */
open class ResolutionTableV2<T: Named>() {

    private val logger = KotlinLogging.logger("ResolutionTable")
    private val actor = CoroutineScope(Dispatchers.IO).tableActor()
    private val resolutionFlow = MutableSharedFlow<ResolutionResult<ResolvedDeclaration<T>>>()
    private val publicResolutionFlow = resolutionFlow.asSharedFlow()

    private fun CoroutineScope.tableActor() = actor<TableMsgV2<T>> {
        var resolutionEnded = false
        val table = HashMap<String, MutableMap<String, MutableList<T>>>()

        for (msg in channel) {
            when (msg) {
                is EndResolutionMsgV2 -> {
                    resolutionFlow.emit(ResolutionEnd())
                    resolutionEnded = true
                }
                is ReadAllTableMessageV2<T> -> {
                    if (resolutionEnded) {
                        msg.response.complete(table)
                    } else {
                        logger.warn {
                            "Tried to read the entire table before the resolution ended, emptyMap being returned..."
                        }
                        msg.response.complete(emptyMap())
                    }
                }
                is TableReadMsgV2<T> -> msg.response.complete(table.containsKey(msg.key))
                is TableWriteMsgV2<T> -> table.computeIfAbsent(msg.key) { mutableMapOf() }
                is TableWriteMethodMsgV2 -> {
                    table.computeIfAbsent(msg.resolved.type) {
                        mutableMapOf()
                    }.computeIfAbsent(msg.resolved.declaration.name) { mutableListOf() }
                        .add(msg.resolved.declaration)
                    resolutionFlow.emit(ResolvedValue(msg.resolved))
                }
                is TableReadMethodMsgV2 ->
                    msg.response.complete(table[msg.key]?.get(msg.methodName).orEmpty())
                is TableReadBySignatureMsgV2 -> {
                    val found = table[msg.key]?.get(msg.signature.name)?.findAny(msg.signature)

                    if (found != null) {
                        msg.response.complete(found)
                    } else {
                        if (resolutionEnded) {
                            msg.response.complete(null)
                        } else {
                            val subscribed = CompletableDeferred<Boolean>()
                            // Do it in another scope to avoid blocking further messages
                            val job = CoroutineScope(Dispatchers.IO).launch {
                                val resolutionResult = publicResolutionFlow.onSubscription {
                                    subscribed.complete(true)
                                }.first {
                                    it is ResolutionEnd<ResolvedDeclaration<T>>
                                            || (it is ResolvedValue<ResolvedDeclaration<T>>
                                            && msg.signature.match(it.value.declaration))
                                }

                                if (resolutionResult is ResolvedValue<ResolvedDeclaration<T>>) {
                                    msg.response.complete(resolutionResult.value.declaration)
                                } else {
                                    msg.response.complete(null)
                                }
                            }

                            // Hmmm.. I'm not sure how to ensure that code inside 'job' already subscribed
                            // to the SharedFlow before unlocking this Coroutine to allow next messages.
                            //
                            // Waits until the subscription occurs to avoid receiving messages before the
                            // subscription happens
                            subscribed.await()
                        }
                    }
                }

            }
        }
    }

    private fun List<T>.findAny(signature: Signature<T>): T? {
        for (method in this) {
            if (signature.match(method)) {
                return method
            }
        }

        return null
    }

    suspend fun lookupFor(
        location: String,
        signature: Signature<T>
    ): T? {
        // TODO: Resolve by assignable types
        val msg = TableReadBySignatureMsgV2<T>(location, signature, CompletableDeferred())
        actor.send(msg)
        return msg.response.await()
    }

    fun subscribers(): StateFlow<Int> =
        this.resolutionFlow.subscriptionCount

    suspend fun registerResolvedDeclaration(resolved: ResolvedDeclaration<T>) {
        actor.send(TableWriteMethodMsgV2(resolved))
    }

    /**
     * Sends a message signaling that the resolution process had ended. Any further message will not be sent
     * to [publicResolutionFlow], an alternative resolution logic should be used as for now all functions are
     * available in the table.
     *
     * Messages to the [actor] are still allowed in order to retrieve resolved methods. From this point, sending
     * [ReadAllTableMessage] is allowed.
     */
    suspend fun endResolution() {
        actor.send(EndResolutionMsgV2.fix())
    }
}

sealed class TableMsgV2<T: Named>
object EndResolutionMsgV2: TableMsgV2<Named>() {
    fun <T: Named> fix(): TableMsgV2<T> = this as TableMsgV2<T>
}
class TableReadMsgV2<T: Named>(
    val key: String,
    val response: CompletableDeferred<Boolean>
): TableMsgV2<T>()

class TableReadMethodMsgV2<T: Named>(
    val key: String,
    val methodName: String,
    val response: CompletableDeferred<List<T>>
): TableMsgV2<T>()

class TableReadBySignatureMsgV2<T: Named>(
    val key: String,
    val signature: Signature<T>,
    val response: CompletableDeferred<T?>
): TableMsgV2<T>()

class TableWriteMsgV2<T: Named>(val key: String): TableMsgV2<T>()
class TableWriteMethodMsgV2<T: Named>(val resolved: ResolvedDeclaration<T>): TableMsgV2<T>()
class ReadAllTableMessageV2<T: Named>(val response: CompletableDeferred<Map<String, Map<String, List<T>>>>): TableMsgV2<T>()