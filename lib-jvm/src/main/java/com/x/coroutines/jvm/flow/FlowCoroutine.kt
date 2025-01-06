@file:OptIn(ExperimentalTypeInference::class)

package com.x.coroutines.jvm.flow

import kotlin.experimental.ExperimentalTypeInference

/*
internal fun <T, R> ScopeCoroutine<T>.startUndispatchedOrReturn(
    receiver: R, block: suspend R.() -> T
): Any? {
    return block.startCoroutineUninterceptedOrReturn(receiver, this)
}

internal suspend fun <R> flowScope(@BuilderInference block: suspend CoroutineScope.() -> R): R =
    suspendCoroutineUninterceptedOrReturn { uCont ->
        val coroutine = FlowCoroutine(uCont.context, uCont)
        coroutine.startUndispatchedOrReturn(coroutine, block)
    }

internal fun <R> scopedFlow(@BuilderInference block: suspend CoroutineScope.(FlowCollector<R>) -> Unit): Flow<R> =
    flow {
        flowScope { block(this@flow) }
    }

private class FlowCoroutine<T>(
    context: CoroutineContext, uCont: Continuation<T>
) : ScopeCoroutine<T>(context, uCont) {
    override fun childCancelled(cause: Throwable): Boolean {
        if (cause is CancellationException && ("Child of the scoped flow was cancelled" == cause.message || (cause.message?.lowercase(
                Locale.getDefault()
            )?.contains("child") == true && cause.message?.lowercase(Locale.getDefault())
                ?.contains("cancel") == true))
        ) return true
        return super.childCancelled(cause)
    }
}
*/