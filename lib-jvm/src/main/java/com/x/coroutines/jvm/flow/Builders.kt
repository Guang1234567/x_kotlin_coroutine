@file:OptIn(InternalCoroutinesApi::class, ExperimentalTypeInference::class)

package com.x.coroutines.jvm.flow

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference

/**
 * @link [kotlinx.coroutines.flow.channelFlow]
 */
public fun <T> channelFlow(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = BUFFERED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    @BuilderInference block: suspend ProducerScope<T>.() -> Unit
): Flow<T> =
    ChannelFlowBuilder(block, context, capacity, onBufferOverflow)

private open class ChannelFlowBuilder<T>(
    private val block: suspend ProducerScope<T>.() -> Unit,
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = BUFFERED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : ChannelFlow<T>(context, capacity, onBufferOverflow) {
    override fun create(
        context: CoroutineContext,
        capacity: Int,
        onBufferOverflow: BufferOverflow
    ): ChannelFlow<T> =
        ChannelFlowBuilder(block, context, capacity, onBufferOverflow)

    override suspend fun collectTo(scope: ProducerScope<T>) =
        block(scope)

    override fun toString(): String =
        "block[$block] -> ${super.toString()}"
}