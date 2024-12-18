# x_kotlin_coroutine

If it weren't for the capabilities of some programming languages such as

- Kotlin's **`Coroutine`**,
- Python's **`Coroutine`**,
- Java's **`Future`** and **`Observable`**,
- Javascript's **`Promise`**,
- and so on ...

, the asynchronous code would be constructed into the infamous Callback hell.

---

<details>
<summary>
This is an example of Callback hell, which becomes more difficult to understand as Nested callbacks increase.
</summary>

```kotlin
fun main() {
    // Simulate fetching user data asynchronously
    fetchUserData { userData ->
        // Result from fetching user data
        println("User data fetched: $userData")

        // Simulate fetching user's order history asynchronously, dependent on user data
        fetchUserOrderHistory(userData) { orderHistory ->
            // Result from fetching user's order history
            println("User's order history fetched: $orderHistory")

            // Simulate fetching detailed order information asynchronously, dependent on order history
            fetchOrderDetails(orderHistory) { orderDetails ->
                // Result from fetching detailed order information
                println("Detailed order information fetched: $orderDetails")

                // Final result
                println("Final result: $orderDetails")

                // Invoke the parent callback (fetchUserOrderHistory's callback)
                processOrderHistory(orderDetails, orderHistory) { processedOrderHistory ->
                    println("Processed order history: $processedOrderHistory")

                    // Invoke the grandparent callback (fetchUserData's callback)
                    processUserData(processedOrderHistory, userData) { processedUserData ->
                        println("Processed user data: $processedUserData")
                    }
                }
            }
        }
    }
}

// Simulate fetching user data asynchronously
fun fetchUserData(callback: (String) -> Unit) {
    // Simulate asynchronous operation with thread sleep
    Thread.sleep(1000)
    callback("User Data")
}

// Simulate fetching user's order history asynchronously
fun fetchUserOrderHistory(userData: String, callback: (String) -> Unit) {
    // Simulate asynchronous operation with thread sleep
    Thread.sleep(1000)
    callback("Order History for $userData")
}

// Simulate fetching detailed order information asynchronously
fun fetchOrderDetails(orderHistory: String, callback: (String) -> Unit) {
    // Simulate asynchronous operation with thread sleep
    Thread.sleep(1000)
    callback("Detailed Order Information for $orderHistory")
}

// Simulate processing the order history
fun processOrderHistory(orderDetails: String, orderHistory: String, callback: (String) -> Unit) {
    // Simulate some processing and invoke the parent callback
    Thread.sleep(1000)
    callback("Processed Order History based on $orderDetails and $orderHistory")
}

// Simulate processing the user data
fun processUserData(processedOrderHistory: String, userData: String, callback: (String) -> Unit) {
    // Simulate some processing and invoke the grandparent callback
    Thread.sleep(1000)
    callback("Processed User Data based on $processedOrderHistory and $userData")
}
```

This example illustrates how invoking parent callbacks within nested callbacks can lead to an
extremely complex and deeply nested structure,
making the code very difficult to read, understand, and maintain.
</details>

---

<details>
<summary>
Using kotlin coroutines can simplify the above asynchronous code and avoid Callback hell.
This is the rewritten code using coroutines, 
which leverages Kotlin's <strong><code>suspend</code></strong> capabilities provided by the <strong><code>kotlin.coroutines.core</code></strong> library.
This allows us to handle asynchronous operations in a more structured and readable way.
</summary>

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    // Simulate fetching user data asynchronously
    val userData = fetchUserData()
    println("User data fetched: $userData")

    // Simulate fetching user's order history asynchronously, dependent on user data
    val orderHistory = fetchUserOrderHistory(userData)
    println("User's order history fetched: $orderHistory")

    // Simulate fetching detailed order information asynchronously, dependent on order history
    val orderDetails = fetchOrderDetails(orderHistory)
    println("Detailed order information fetched: $orderDetails")

    // Final result
    println("Final result: $orderDetails")

    // Simulate processing the order history
    val processedOrderHistory = processOrderHistory(orderDetails, orderHistory)
    println("Processed order history: $processedOrderHistory")

    // Simulate processing the user data
    val processedUserData = processUserData(processedOrderHistory, userData)
    println("Processed user data: $processedUserData")
}

// Simulate fetching user data asynchronously
suspend fun fetchUserData(): String {
    delay(1000) // Simulate asynchronous operation with delay
    return "User Data"
}

// Simulate fetching user's order history asynchronously
suspend fun fetchUserOrderHistory(userData: String): String {
    delay(1000) // Simulate asynchronous operation with delay
    return "Order History for $userData"
}

// Simulate fetching detailed order information asynchronously
suspend fun fetchOrderDetails(orderHistory: String): String {
    delay(1000) // Simulate asynchronous operation with delay
    return "Detailed Order Information for $orderHistory"
}

// Simulate processing the order history
suspend fun processOrderHistory(orderDetails: String, orderHistory: String): String {
    delay(1000) // Simulate some processing with delay
    return "Processed Order History based on $orderDetails and $orderHistory"
}

// Simulate processing the user data
suspend fun processUserData(processedOrderHistory: String, userData: String): String {
    delay(1000) // Simulate some processing with delay
    return "Processed User Data based on $processedOrderHistory and $userData"
}
```

</details>

### Operators

Using the following operators can enhance our ability to handle various complex asynchronous
business scenarios in a more highly efficient and simple way.

The following operators is grouped by usage scenarios.

- Operators for switching thread scenarios

<details>
<summary><strong>continueOn</strong></summary>

- Usage

```kotlin
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
```

- Output

```agsl
 HomeFragment            com.x.coroutines                     W  ============================case 1=================================
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = A
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = main
 HomeFragment            com.x.coroutines                     W  ============================case 2=================================
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = B
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = main
 HomeFragment            com.x.coroutines                     W  ============================case 3=================================
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = main
 HomeFragment            com.x.coroutines                     W  ============================case 4=================================
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = A
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = A
 HomeFragment            com.x.coroutines                     W  ============================case 5=================================
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = B
 HomeFragment            com.x.coroutines                     I  withContext(ctxC) {
                                                                 	Thread.currentThread().name = C
 HomeFragment            com.x.coroutines                     I  	Thread.currentThread().name = A
 HomeFragment            com.x.coroutines                     I  	Thread.currentThread().name = D
 HomeFragment            com.x.coroutines                     I  	withContext(ctxC) {
                                                                 		Thread.currentThread().name = C
 HomeFragment            com.x.coroutines                     I  		Thread.currentThread().name = B
                                                                 	}
 HomeFragment            com.x.coroutines                     I  	Thread.currentThread().name = D
                                                                 }
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = B
 HomeFragment            com.x.coroutines                     W  ============================case 6=================================
 HomeFragment            com.x.coroutines                     I  async {
                                                                 	Thread.currentThread().name = main
 HomeFragment            com.x.coroutines                     I  	Thread.currentThread().name = D
                                                                 }
 HomeFragment            com.x.coroutines                     I  async {} . name = D
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = B
 HomeFragment            com.x.coroutines                     W  ============================case 7=================================
 HomeFragment            com.x.coroutines                     I  async {
                                                                 	Thread.currentThread().name = A
 HomeFragment            com.x.coroutines                     I  	Thread.currentThread().name = C
                                                                 }
 HomeFragment            com.x.coroutines                     I  async {} . name2 = C
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = B
 HomeFragment            com.x.coroutines                     W  ============================case 8=================================
 HomeFragment            com.x.coroutines                     I  async {
                                                                 	Thread.currentThread().name = C
 HomeFragment            com.x.coroutines                     I  	Thread.currentThread().name = C
                                                                 }
 HomeFragment            com.x.coroutines                     I  async {} . name3 = C
 HomeFragment            com.x.coroutines                     I  Thread.currentThread().name = C
```

</details>

- To be continued ...