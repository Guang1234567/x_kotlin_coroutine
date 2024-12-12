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
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce

import com.x.coroutines.databinding.FragmentHomeBinding
import com.x.coroutines.jvm.operators.continueOn
import com.x.coroutines.android.flow.observeOn
import com.x.coroutines.android.flow.viewLifecycleScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

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
        homeViewModel.text
            .debounce(150)
            .conflate()
            .observeOn(fragment = this@HomeFragment) {
                textView.text = it

                Log.w(
                    "HomeFragment",
                    "============================case 1================================="
                )

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
                        continueOn(ctxC)
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
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}