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
import com.x.coroutines.android.flow.viewLifecycleScope
import com.x.coroutines.databinding.FragmentHomeBinding
import com.x.coroutines.jvm.flow.operators.bufferTimeout
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.newSingleThreadContext
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
        homeViewModel.text2.observe(viewLifecycleOwner) {
            textView.text = it
        }

        textView.clicksCountInFixedTimeWindow()
            .onEach {
                Log.w(
                    "HomeFragment",
                    "Clicked $it times !"
                )
            }.launchIn(this@HomeFragment.viewLifecycleScope)
        /*.observeOn(this@HomeFragment) {
            Log.w(
                "HomeFragment",
                "Clicked $it times !"
            )
        }*/


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun View.clicksCountInFixedTimeWindow(timeout: Duration = 3000.milliseconds): Flow<Int> =
    clicks()
        .bufferTimeout(7u, timeout)
        .map { it.size }