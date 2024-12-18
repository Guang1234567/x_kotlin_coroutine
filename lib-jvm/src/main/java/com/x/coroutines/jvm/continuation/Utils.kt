package com.x.coroutines.jvm.continuation

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

/**
 * [kotlin.coroutines.jvm.internal.ContinuationImpl]
 */
private val coroutineImplClass by lazy { Class.forName("kotlin.coroutines.jvm.internal.ContinuationImpl") }

/**
 * [kotlinx.coroutines.CancellableContinuationImpl]
 */
private val cancellableContinuationImplClass by lazy { Class.forName("kotlinx.coroutines.CancellableContinuationImpl") }

/**
 * [kotlinx.coroutines.DispatchedTask]
 */
private val dispatchedTaskClass by lazy { Class.forName("kotlinx.coroutines.DispatchedTask") }

/**
 * [kotlinx.coroutines.DispatchedContinuation]
 */
private val dispatchedContinuationClass by lazy { Class.forName("kotlinx.coroutines.internal.DispatchedContinuation") }

private val _contextField by lazy {
    coroutineImplClass.getDeclaredField("_context").apply { isAccessible = true }
}

private val _delegateField by lazy {
    cancellableContinuationImplClass.getDeclaredField("delegate").apply { isAccessible = true }
}

private val _resumeModeField by lazy {
    dispatchedTaskClass.getDeclaredField("resumeMode").apply { isAccessible = true }
}

private val _continuationField by lazy {
    dispatchedContinuationClass.getDeclaredField("continuation").apply { isAccessible = true }
}

private val <T> Continuation<T>._delegateInstanceOfCancellableContinuationImpl: Continuation<T>
    get() = _delegateField.get(this@_delegateInstanceOfCancellableContinuationImpl) as Continuation<T>

private val <T> Continuation<T>._continuationInstanceOfDispatchedContinuation: Continuation<T>
    get() = _continuationField.get(this@_continuationInstanceOfDispatchedContinuation) as Continuation<T>

internal var <T> Continuation<T>._context: CoroutineContext?
    get() = this.context
    set(value) {
        if (cancellableContinuationImplClass.isInstance(this@_context)) {
            _resumeModeField.set(
                this@_context, 4
                /** [kotlinx.coroutines.MODE_UNDISPATCHED] */
            )
            _resumeModeField.set(
                this@_context._delegateInstanceOfCancellableContinuationImpl, 4
                /** [kotlinx.coroutines.MODE_UNDISPATCHED] */
            )
            _contextField.set(
                this@_context._delegateInstanceOfCancellableContinuationImpl._continuationInstanceOfDispatchedContinuation,
                value
            )
        } else {
            _contextField.set(this@_context, value)
        }
    }

private val _interceptedField by lazy {
    coroutineImplClass.getDeclaredField("intercepted").apply { isAccessible = true }
}

internal var <T> Continuation<T>._intercepted: Continuation<*>?
    get() {
        _interceptedField.get(this) as Continuation<*>
        if (cancellableContinuationImplClass.isInstance(this@_intercepted)) {
            return _interceptedField.get(this@_intercepted._delegateInstanceOfCancellableContinuationImpl) as Continuation<*>
        } else {
            return _interceptedField.get(this@_intercepted) as Continuation<*>
        }
    }
    set(value) {
        if (cancellableContinuationImplClass.isInstance(this@_intercepted)) {
            //_resumeModeField.set(this@_context, 4 /** [kotlinx.coroutines.MODE_UNDISPATCHED] */)
            //_resumeModeField.set(this@_context._delegateInstanceOfCancellableContinuationImpl, 4 /** [kotlinx.coroutines.MODE_UNDISPATCHED] */)
            _interceptedField.set(
                this@_intercepted._delegateInstanceOfCancellableContinuationImpl._continuationInstanceOfDispatchedContinuation,
                value
            )
        } else {
            _interceptedField.set(this@_intercepted, value)
        }
    }