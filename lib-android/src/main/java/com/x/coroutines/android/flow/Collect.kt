@file:OptIn(FlowPreview::class, ExperimentalTypeInference::class)

package com.x.coroutines.android.flow

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalTypeInference

inline val Fragment.viewLifecycle: Lifecycle
    get() = viewLifecycleOwner.lifecycle

inline val Fragment.viewLifecycleScope: LifecycleCoroutineScope
    get() = viewLifecycle.coroutineScope

private fun <T> Flow<T>.observeOnImpl(
    owner: LifecycleOwner,
    context: CoroutineContext = Dispatchers.Main,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend LifecycleCoroutineScope.(T) -> Unit,
): Job = this.asLifecycleFlowImpl(owner)
    .flowWithLifecycle(minActiveState = minActiveState)
    .flowOn(context)
    .onEach(action)
    .launch()

fun <T> Flow<T>.observeOn(
    fragment: Fragment,
    context: CoroutineContext = Dispatchers.Main,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend LifecycleCoroutineScope.(T) -> Unit,
): Job = observeOnImpl(fragment.viewLifecycleOwner, context, minActiveState, action)

fun <T> Flow<T>.observeOn(
    activity: ComponentActivity,
    context: CoroutineContext = Dispatchers.Main,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend LifecycleCoroutineScope.(T) -> Unit,
): Job = observeOnImpl(activity, context, minActiveState, action)

private fun <T> Flow<T>.asLifecycleFlowImpl(owner: LifecycleOwner): LifecycleFlow<T> =
    LifecycleFlow(upFlow = this, owner)

fun <T> Flow<T>.asLifecycleFlow(
    fragment: Fragment
): LifecycleFlow<T> = asLifecycleFlowImpl(fragment.viewLifecycleOwner)

fun <T> Flow<T>.asLifecycleFlow(
    activity: ComponentActivity
): LifecycleFlow<T> = asLifecycleFlowImpl(activity)

class LifecycleFlow<T>(private val upFlow: Flow<T>, private val owner: LifecycleOwner) {

    private inline fun <R> intercepted(collect: Flow<T>.() -> R): R = upFlow.collect()

    private inline fun <R> intercepted(map: Flow<T>.() -> Flow<R>): LifecycleFlow<R> =
        intercepted(collect = c@{
            LifecycleFlow(
                this@c.map(),
                this@LifecycleFlow.owner
            )
        })

    fun flowWithLifecycle(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED
    ): LifecycleFlow<T> = intercepted {
        flowWithLifecycle(lifecycle = owner.lifecycle, minActiveState)
    }

    fun flowOn(context: CoroutineContext): LifecycleFlow<T> = intercepted {
        flowOn(context)
    }

    fun flowOnMain(): LifecycleFlow<T> = intercepted {
        flowOn(Dispatchers.Main)
    }

    fun flowOnIO(): LifecycleFlow<T> = intercepted {
        flowOn(Dispatchers.IO)
    }

    fun debounce(timeoutMillis: Long): LifecycleFlow<T> = intercepted {
        debounce(timeoutMillis)
    }

    fun conflate(): LifecycleFlow<T> = intercepted {
        conflate()
    }

    fun onEach(action: suspend LifecycleCoroutineScope.(T) -> Unit): LifecycleFlow<T> =
        intercepted {
            onEach {
                owner.lifecycleScope.action(it)
            }
        }

    fun <R> transform(
        @BuilderInference transform: suspend FlowCollector<R>.(value: T) -> Unit
    ): LifecycleFlow<R> = intercepted {
        transform(transform)
    }

    fun launch(): Job = intercepted(collect = { launchIn(this@LifecycleFlow.owner.lifecycleScope) })
}

