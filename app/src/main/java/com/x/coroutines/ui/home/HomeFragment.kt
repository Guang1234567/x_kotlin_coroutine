@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class, FlowPreview::class)

package com.x.coroutines.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.x.coroutines.android.flow.asLifecycleFlow
import com.x.coroutines.android.flow.observeOn
import com.x.coroutines.android.flow.viewLifecycleScope
import com.x.coroutines.databinding.FragmentHomeBinding
import com.x.coroutines.jvm.operators.bufferTimeout
import com.x.coroutines.jvm.operators.continueOn
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import reactivecircus.flowbinding.android.view.clicks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val ctxA = newSingleThreadContext("A")
    private val ctxB = newSingleThreadContext("B")
    private val ctxC = newSingleThreadContext("C")
    private val ctxD = newSingleThreadContext("D")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        /*homeViewModel.text2.observe(viewLifecycleOwner) {
            textView.text = it
        }*/

        textView.clicksCountInFixedTimeWindow()
            /*.onEach {
                Log.w(
                    "HomeFragment",
                    "Clicked $it times !"
                )
            }.launchIn(this@HomeFragment.viewLifecycleScope)*/
            .observeOn(this@HomeFragment) {
                Log.w(
                    "HomeFragment",
                    "Clicked $it times !"
                )
            }

        homeViewModel.text
            // ---  点击事件可以用一下 👇, 如果是类似于微信那种每一条消息都要显示的情况就不要用了.
            .debounce(150)
            .conflate()
            // ---  点击事件可以用一下 👆
            .asLifecycleFlow(fragment = this@HomeFragment)
            .flowWithLifecycle()
            .flowOnMain()
            .onEach {
                textView.text = it

                Log.w(
                    "HomeFragment",
                    "============================case 1================================="
                )
                /*launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
                    Log.w(
                        "HomeFragment",
                        "Unconfined      : I'm working in thread ${Thread.currentThread().name}"
                    )
                    delay(500)
                    Log.w(
                        "HomeFragment",
                        "Unconfined      : After delay in thread ${Thread.currentThread().name}"
                    )
                }*/

                withContext(ctxA) {
                    Log.i(
                        "HomeFragment",
                        "Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                }
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )


                Log.w(
                    "HomeFragment",
                    "============================case 2================================="
                )


                withContext(ctxB) {
                    Log.i(
                        "HomeFragment",
                        "Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                }
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                Log.w(
                    "HomeFragment",
                    "============================case 3================================="
                )

                continueOn(Dispatchers.Main.immediate)
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                Log.w(
                    "HomeFragment",
                    "============================case 4================================="
                )

                continueOn(ctxA)
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )
                continueOn(ctxA)
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                Log.w(
                    "HomeFragment",
                    "============================case 5================================="
                )

                /*continueOn(ctxC)
                Log.i(
                    "HomeFragment",
                    "88Thread.currentThread().name = ${Thread.currentThread().name}"
                )*/

                continueOn(ctxB)
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                withContext(ctxC) {
                    Log.i(
                        "HomeFragment",
                        "withContext(ctxC) {\n\tThread.currentThread().name = ${Thread.currentThread().name}"
                    )
                    continueOn(ctxA)
                    Log.i(
                        "HomeFragment",
                        "\tThread.currentThread().name = ${Thread.currentThread().name}\n"
                    )
                    continueOn(ctxD)
                    Log.i(
                        "HomeFragment",
                        "\tThread.currentThread().name = ${Thread.currentThread().name}\n"
                    )

                    withContext(ctxC) {
                        Log.i(
                            "HomeFragment",
                            "\twithContext(ctxC) {\n\t\tThread.currentThread().name = ${Thread.currentThread().name}"
                        )
                        continueOn(ctxB)
                        Log.i(
                            "HomeFragment",
                            "\t\tThread.currentThread().name = ${Thread.currentThread().name}\n\t}"
                        )
                    }

                    Log.i(
                        "HomeFragment",
                        "\tThread.currentThread().name = ${Thread.currentThread().name}\n}"
                    )
                }
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                Log.w(
                    "HomeFragment",
                    "============================case 6================================="
                )

                val name = async {
                    Log.i(
                        "HomeFragment",
                        "async {\n\tThread.currentThread().name = ${Thread.currentThread().name}"
                    )
                    continueOn(ctxD)
                    Log.i(
                        "HomeFragment",
                        "\tThread.currentThread().name = ${Thread.currentThread().name}\n}"
                    )
                    return@async Thread.currentThread().name
                }.await()

                Log.i(
                    "HomeFragment",
                    "async {} . name = $name"
                )
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                Log.w(
                    "HomeFragment",
                    "============================case 7================================="
                )

                val name2 = async(ctxA) {
                    Log.i(
                        "HomeFragment",
                        "async {\n\tThread.currentThread().name = ${Thread.currentThread().name}"
                    )
                    continueOn(ctxC)
                    Log.i(
                        "HomeFragment",
                        "\tThread.currentThread().name = ${Thread.currentThread().name}\n}"
                    )
                    return@async Thread.currentThread().name
                }.await()

                Log.i(
                    "HomeFragment",
                    "async {} . name2 = $name2"
                )
                Log.i(
                    "HomeFragment",
                    "Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                Log.w(
                    "HomeFragment",
                    "============================case 8================================="
                )

                withContext(ctxC) {
                    val name3 = async {
                        Log.i(
                            "HomeFragment",
                            "async {\n\tThread.currentThread().name = ${Thread.currentThread().name}"
                        )
                        continueOn(ctxA)
                        Log.i(
                            "HomeFragment",
                            "\tThread.currentThread().name = ${Thread.currentThread().name}\n}"
                        )
                        return@async Thread.currentThread().name
                    }.await()

                    Log.i(
                        "HomeFragment",
                        "async {} . name3 = $name3"
                    )
                    Log.i(
                        "HomeFragment",
                        "Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                }
            }
            .launch()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun View.clicksCountInFixedTimeWindow(timeout: Duration = 3000.milliseconds): Flow<Int> =
    clicks().bufferTimeout(
        7u, timeout
    ).map { it.size }