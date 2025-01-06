@file:OptIn(InternalCoroutinesApi::class, ExperimentalTypeInference::class)

package com.x.coroutines.jvm.flow

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference

internal fun <R> scopedFlow(@BuilderInference block: suspend CoroutineScope.(FlowCollector<R>) -> Unit): Flow<R> =
    flow {
        coroutineScope { block(this@flow) }
    }

public fun <T> channelFlow(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCompletion: CompletionHandler? = null,
    @BuilderInference block: suspend ProducerScope<T>.(ProducerScope<T>) -> Unit
): Flow<T> = scopedFlow { downstream ->
    //val channel = Channel<String>(Channel.RENDEZVOUS)
    val channel: ReceiveChannel<T> =
        produce(context, capacity, start, onCompletion) { block(this, this) }
    channel.consumeEach {
        downstream.emit(it)
    }
}

/**
 * @link [kotlinx.coroutines.flow.channelFlow]
 */
public fun <T> channelFlow2(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = RENDEZVOUS,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    @BuilderInference block: suspend ProducerScope<T>.(ProducerScope<T>) -> Unit
): Flow<T> =
    ChannelFlowBuilder(block, context, capacity, onBufferOverflow)

private open class ChannelFlowBuilder<T>(
    private val block: suspend ProducerScope<T>.(ProducerScope<T>) -> Unit,
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = RENDEZVOUS,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : ChannelFlow<T>(context, capacity, onBufferOverflow) {

    override fun create(
        context: CoroutineContext,
        capacity: Int,
        onBufferOverflow: BufferOverflow
    ): ChannelFlow<T> {
        println(".....onBufferOverflow = $onBufferOverflow   capacity = $capacity")
        return ChannelFlowBuilder(block, context, capacity, onBufferOverflow)
    }

    override suspend fun collectTo(scope: ProducerScope<T>) =
        block(scope, scope)

    override fun toString(): String =
        "block[$block] -> ${super.toString()}"
}