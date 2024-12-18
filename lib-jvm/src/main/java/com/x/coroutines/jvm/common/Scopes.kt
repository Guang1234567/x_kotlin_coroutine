/*
@file:OptIn(InternalCoroutinesApi::class)

package com.x.coroutines.jvm.common

import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.internal.resumeCancellableWith
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.jvm.internal.CoroutineStackFrame

/**
 * This is a coroutine instance that is created by [coroutineScope] builder.
 */
internal open class ScopeCoroutine<in T>(
    context: CoroutineContext,
    @JvmField val uCont: Continuation<T> // unintercepted continuation
) : AbstractCoroutine<T>(context, true, true), CoroutineStackFrame {

    final override val callerFrame: CoroutineStackFrame? get() = uCont as? CoroutineStackFrame
    final override fun getStackTraceElement(): StackTraceElement? = null

    final override val isScopedCoroutine: Boolean get() = true

    override fun afterCompletion(state: Any?) {
        // Resume in a cancellable way by default when resuming from another context
        uCont.intercepted().resumeCancellableWith(recoverResult(state, uCont))
    }

    override fun afterResume(state: Any?) {
        // Resume direct because scope is already in the correct context
        uCont.resumeWith(recoverResult(state, uCont))
    }
}

internal fun <T> recoverResult(state: Any?, uCont: Continuation<T>): Result<T> =
    if (state is Throwable)
        Result.failure(state)
    else
        Result.success(state as T)

 */