/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.randeep.moviedb.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout that throws an exception.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 *
 * @param time The time to await for
 * @param timeUnit The time unit to await for
 * @param afterObserve This is invoked after calling observe but before awaiting.
 * Use this to call the function which will update the LiveData
 */
fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        afterObserve: (() -> Unit)? = null
): T? {
        val valueList = this.getOrAwaitMultipleValues(1, time, timeUnit, afterObserve)
        return valueList[0]
}

/**
 * Waits for the expected number of values from a [LiveData], with a timeout that throws an exception.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 *
 * @param howMany The expected number of times setValue should be called
 * @param time The time to await for
 * @param timeUnit The time unit to await for
 * @param afterObserve This is invoked after calling observe but before awaiting.
 * Use this to call the function which will update the LiveData
 */
fun <T> LiveData<T>.getOrAwaitMultipleValues(
        howMany: Int,
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        afterObserve: (() -> Unit)? = null
): List<T?> {
        val valueList = mutableListOf<T?>()
        val latch = CountDownLatch(howMany)
        val observer = object : Observer<T> {

                override fun onChanged(value: T) {
                        valueList.add(value)
                        latch.countDown()
                        if (latch.count == 0L) {
                                this@getOrAwaitMultipleValues.removeObserver(this)
                        }
                }
        }
        this.observeForever(observer)

        afterObserve?.invoke()

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
                this.removeObserver(observer)
                throw TimeoutException("LiveData value was set ${valueList.size} times, expected $howMany times")
        }

        return valueList
}

/**
 * Observes a [LiveData] until the `block` is done executing.
 */
fun <T> LiveData<T>.observeForTesting(block: () -> Unit) {
        val observer = Observer<T> { }
        try {
                observeForever(observer)
                block()
        } finally {
                removeObserver(observer)
        }
}