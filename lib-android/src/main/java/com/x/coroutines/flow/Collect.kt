package com.x.coroutines.flow

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

inline val Fragment.viewLifecycle: Lifecycle
    get() = viewLifecycleOwner.lifecycle

inline val Fragment.viewLifecycleScope: LifecycleCoroutineScope
    get() = viewLifecycle.coroutineScope

/*
inline val LifecycleOwner.lifecycleOwner: LifecycleOwner
    get() = this
*/

private fun <T> Flow<T>.observeOn(
    owner: LifecycleOwner,
    context: CoroutineContext = Dispatchers.Main,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job =
    flowOn(context)
        .flowWithLifecycle(lifecycle = owner.lifecycle, minActiveState = minActiveState)
        .onEach(action)
        .launchIn(owner.lifecycleScope)

fun <T> Flow<T>.observeOn(
    fragment: Fragment,
    context: CoroutineContext = Dispatchers.Main,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job = observeOn(fragment.viewLifecycleOwner, context, minActiveState, action)

fun <T> Flow<T>.observeOn(
    activity: ComponentActivity,
    context: CoroutineContext = Dispatchers.Main,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job = observeOn(activity, context, minActiveState, action)