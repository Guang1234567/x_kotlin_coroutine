@file:OptIn(InternalCoroutinesApi::class)

package com.x.coroutines.jvm.operators


import com.x.coroutines.jvm.continuation.SafeContinuation
import com.x.coroutines.jvm.continuation._context
import com.x.coroutines.jvm.continuation._intercepted
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.intrinsics.startCoroutineCancellable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

suspend fun continueOn(
    dispatcher: CoroutineDispatcher
): Unit {
    // uCont 就是代指`当前协程`
    // Step 1 : suspendCoroutineForContinueOn 的作用就是把当前协程 uCont 进入 suspend 状态, 此时在 Step 2 处`暂停执行`.
    return suspendCoroutineUninterceptedOrReturn sc@{ unSafeCont ->
        // compute new context
        val oldContext = unSafeCont.context

        // FAST PATH #1 -- the new dispatcher is as same as the old one is.
        if (dispatcher == oldContext[ContinuationInterceptor]) {
            /*Log.w(
                "continueOn", "CoroutineDispatcher 没有变化不切换 ${Thread.currentThread().name}"
            )*/
            //
            //unSafeCont.resumeWith(Result.success(Unit))
            return@sc Unit
        } else {
            // Copy CopyableThreadContextElement if necessary
            val newContext = oldContext + dispatcher
            // always check for cancellation of new context
            newContext.ensureActive()

            val newCoroutine = ContinueOnContinuation(newContext, unSafeCont)

            val block: suspend () -> Unit = b@{
                /*Log.w(
                    "continueOn", "正在切换到 ${Thread.currentThread().name}"
                )*/

                return@b Unit
            }

            // SLOW PATH -- use new dispatcher
            block.startCoroutineCancellable(newCoroutine)

            kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
        }
    }
    // Step 2
}

internal class ContinueOnContinuation<in T>(
    override val context: CoroutineContext, @JvmField val uCont: Continuation<T>
) : Continuation<T> {

    override fun resumeWith(result: Result<T>) {
        /*Log.w(
            "continueOn", "成功切换到 ${Thread.currentThread().name}"
        )*/

        uCont._context = context
        uCont._intercepted = null
        // Step 3 : 通知 uCont 从 Step 2 处`恢复执行`
        uCont.resumeWith(result)
    }
}

@OptIn(ExperimentalContracts::class)
internal suspend inline fun <T> suspendCoroutineForContinueOn(crossinline block: (SafeContinuation<T>) -> Unit): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return suspendCoroutineUninterceptedOrReturn { c: Continuation<T> ->
        // `c.intercepted()` means to switch back to the original dispatcher
        //val safe = SafeContinuation(c.intercepted())
        val safe = SafeContinuation(c)
        block(safe)
        safe.getOrThrow()
    }
}