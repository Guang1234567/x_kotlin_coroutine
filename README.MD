# x_kotlin_coroutine

// TODO:

## `continueOn` Operator

- Usage

```kotlin
@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package com.x.coroutines.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce

import com.x.coroutines.databinding.FragmentHomeBinding
import android.flow.continueOn
import android.flow.observeOn
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
                withContext(ctxA) {
                    Log.i(
                        "HomeFragment",
                        "1Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                }
                Log.i(
                    "HomeFragment",
                    "2Thread.currentThread().name = ${Thread.currentThread().name}"
                )
                withContext(ctxB) {
                    Log.i(
                        "HomeFragment",
                        "3Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                }
                Log.i(
                    "HomeFragment",
                    "4Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                continueOn(Dispatchers.Main.immediate)
                Log.i(
                    "HomeFragment",
                    "5Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                continueOn(ctxA)
                Log.i(
                    "HomeFragment",
                    "6Thread.currentThread().name = ${Thread.currentThread().name}"
                )
                continueOn(ctxA)
                Log.i(
                    "HomeFragment",
                    "7Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                /*continueOn(ctxC)
                Log.i(
                    "HomeFragment",
                    "88Thread.currentThread().name = ${Thread.currentThread().name}"
                )*/

                continueOn(ctxB)
                Log.i(
                    "HomeFragment",
                    "8Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                withContext(ctxC) {
                    Log.i(
                        "HomeFragment",
                        "withContext(ctxC) {\n\t9Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                    continueOn(ctxA)
                    Log.i(
                        "HomeFragment",
                        "\t10Thread.currentThread().name = ${Thread.currentThread().name}\n"
                    )
                    continueOn(ctxD)
                    Log.i(
                        "HomeFragment",
                        "\t10Thread.currentThread().name = ${Thread.currentThread().name}\n"
                    )

                    withContext(ctxC) {
                        Log.i(
                            "HomeFragment",
                            "\twithContext(ctxC) {\n\t\t9Thread.currentThread().name = ${Thread.currentThread().name}"
                        )
                        continueOn(ctxB)
                        Log.i(
                            "HomeFragment",
                            "\t\t10Thread.currentThread().name = ${Thread.currentThread().name}\n\t}"
                        )
                    }

                    Log.i(
                        "HomeFragment",
                        "\t10Thread.currentThread().name = ${Thread.currentThread().name}\n}"
                    )
                }
                Log.i(
                    "HomeFragment",
                    "11Thread.currentThread().name = ${Thread.currentThread().name}"
                )

                val name = this@HomeFragment.viewLifecycleOwner.lifecycleScope.async {
                    Log.i(
                        "HomeFragment",
                        "async {\n\t9Thread.currentThread().name = ${Thread.currentThread().name}"
                    )
                    continueOn(ctxD)
                    Log.i(
                        "HomeFragment",
                        "\t10Thread.currentThread().name = ${Thread.currentThread().name}\n}"
                    )
                    return@async Thread.currentThread().name
                }.await()

                Log.i(
                    "HomeFragment",
                    "async {} . name = $name"
                )
                Log.i(
                    "HomeFragment",
                    "11Thread.currentThread().name = ${Thread.currentThread().name}"
                )
            }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

- Output

```agsl
  HomeFragment            com.x.coroutines                     I  1Thread.currentThread().name = A
  HomeFragment            com.x.coroutines                     I  2Thread.currentThread().name = main
  HomeFragment            com.x.coroutines                     I  3Thread.currentThread().name = B
  HomeFragment            com.x.coroutines                     I  4Thread.currentThread().name = main
  HomeFragment            com.x.coroutines                     I  5Thread.currentThread().name = main
  HomeFragment            com.x.coroutines                     I  6Thread.currentThread().name = A
  HomeFragment            com.x.coroutines                     I  7Thread.currentThread().name = A
  HomeFragment            com.x.coroutines                     I  8Thread.currentThread().name = B
  HomeFragment            com.x.coroutines                     I  withContext(ctxC) {
                                                                  	9Thread.currentThread().name = C
  HomeFragment            com.x.coroutines                     I  	10Thread.currentThread().name = A
  HomeFragment            com.x.coroutines                     I  	10Thread.currentThread().name = D
  HomeFragment            com.x.coroutines                     I  	withContext(ctxC) {
                                                                  		9Thread.currentThread().name = C
  HomeFragment            com.x.coroutines                     I  		10Thread.currentThread().name = B
                                                                  	}
  HomeFragment            com.x.coroutines                     I  	10Thread.currentThread().name = D
                                                                  }
  HomeFragment            com.x.coroutines                     I  11Thread.currentThread().name = B
  HomeFragment            com.x.coroutines                     I  async {
                                                                  	9Thread.currentThread().name = main
  HomeFragment            com.x.coroutines                     I  	10Thread.currentThread().name = D
                                                                  }
  HomeFragment            com.x.coroutines                     I  async {} . name = D
  HomeFragment            com.x.coroutines                     I  11Thread.currentThread().name = B
```