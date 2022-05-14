package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred

class Verify<out C, out R>(val timeoutMs: Long, private val runTestAsync: (suspend C.(R) -> Unit) -> Deferred<Unit>) {
    infix fun teardown(function: suspend C.(R?) -> Unit) = finalTransform(timeoutMs) { runTestAsync(function) }
}
