package com.x.coroutines.jvm.continuation

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

private val coroutineImplClass by lazy { Class.forName("kotlin.coroutines.jvm.internal.ContinuationImpl") }

private val _contextField by lazy {
    coroutineImplClass.getDeclaredField("_context").apply { isAccessible = true }
}

internal var <T> Continuation<T>._context: CoroutineContext?
    get() = _contextField.get(this) as CoroutineContext
    set(value) = _contextField.set(this@_context, value)

private val _interceptedField by lazy {
    coroutineImplClass.getDeclaredField("intercepted").apply { isAccessible = true }
}

internal var <T> Continuation<T>._intercepted: Continuation<*>?
    get() = _interceptedField.get(this) as Continuation<*>
    set(value) = _interceptedField.set(this@_intercepted, value)