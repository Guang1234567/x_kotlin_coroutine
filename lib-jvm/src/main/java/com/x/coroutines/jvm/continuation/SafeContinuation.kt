package com.x.coroutines.jvm.continuation

import com.x.coroutines.jvm.continuation.CoroutineSingletons.RESUMED
import com.x.coroutines.jvm.continuation.CoroutineSingletons.UNDECIDED
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.jvm.internal.CoroutineStackFrame

private enum class CoroutineSingletons { /*COROUTINE_SUSPENDED,*/ UNDECIDED, RESUMED }

private val COROUTINE_SUSPENDED: Any get() = kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED


class SafeContinuation<in T>
internal constructor(
    private val delegate: Continuation<T>, initialResult: Any?
) : Continuation<T>, CoroutineStackFrame {
    constructor(delegate: Continuation<T>) : this(delegate, UNDECIDED)

    override val context: CoroutineContext
        get() = delegate.context

    @Volatile
    private var result: Any? = initialResult

    private companion object {
        @Suppress("UNCHECKED_CAST")
        private val RESULT = AtomicReferenceFieldUpdater.newUpdater<SafeContinuation<*>, Any?>(
            SafeContinuation::class.java, Any::class.java as Class<Any?>, "result"
        )
    }

    override fun resumeWith(result: Result<T>) {
        while (true) { // lock-free loop
            val cur = this.result // atomic read
            when {
                cur === UNDECIDED -> if (RESULT.compareAndSet(
                        this, UNDECIDED, result.getOrNull()!!
                    )
                ) return

                cur === COROUTINE_SUSPENDED -> if (RESULT.compareAndSet(
                        this, COROUTINE_SUSPENDED, RESUMED
                    )
                ) {
                    delegate.resumeWith(result)
                    return
                }

                else -> throw IllegalStateException("Already resumed")
            }
        }
    }

    @PublishedApi
    internal fun getOrThrow(): Any? {
        var result = this.result // atomic read
        if (result === UNDECIDED) {
            if (RESULT.compareAndSet(
                    this, UNDECIDED, COROUTINE_SUSPENDED
                )
            ) return COROUTINE_SUSPENDED
            result = this.result // reread volatile var
        }
        return when {
            result === RESUMED -> COROUTINE_SUSPENDED // already called continuation, indicate COROUTINE_SUSPENDED upstream
            result is Result<*> && result.isFailure -> throw result.exceptionOrNull()!!
            else -> result // either COROUTINE_SUSPENDED or data
        }
    }

    // --- CoroutineStackFrame implementation

    override val callerFrame: CoroutineStackFrame?
        get() = delegate as? CoroutineStackFrame

    override fun getStackTraceElement(): StackTraceElement? = null

    override fun toString(): String =
        "com.x.coroutines.android.android.flow.SafeContinuation for $delegate"
}