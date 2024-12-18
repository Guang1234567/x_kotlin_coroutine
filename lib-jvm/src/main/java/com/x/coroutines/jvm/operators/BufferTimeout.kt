@file:OptIn(ExperimentalCoroutinesApi::class)

package com.x.coroutines.jvm.operators

import com.x.coroutines.jvm.flow.scopedFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.whileSelect
import kotlin.time.Duration

fun <T> Flow<T>.bufferTimeout(capacity: UInt, window: Duration): Flow<List<T>> =
    scopedFlow { downStream ->
        val upStream = this@bufferTimeout
        val size: Int = capacity.toInt()

        val events = ArrayList<T>(size)
        val upstreamChannel =
            upStream.buffer(Channel.RENDEZVOUS).onCompletion { }
                .produceIn(this@scopedFlow)

        suspend fun FlowCollector<List<T>>.emitThenClear() {
            emit(events.take(size))
            events.clear()
        }

        whileSelect {
            upstreamChannel.onReceiveCatching { value ->
                value.onSuccess {
                    events.add(it)
                    if (events.size >= size) {
                        downStream.emitThenClear()
                    }
                }.onClosed {
                    return@onReceiveCatching false
                }
                return@onReceiveCatching true
            }

            onTimeout(window) {
                if (events.isNotEmpty()) {
                    downStream.emitThenClear()
                }
                return@onTimeout true
            }
        }
    }