package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.test.runTest

fun finalTransform(timeoutMs: Long, deferred: () -> Deferred<Unit>) = runTest(dispatchTimeoutMs = timeoutMs) {
    deferred().await()
}

expect suspend fun waitForTest(testFunction: () -> Unit)

expect fun <T> eventLoopProtect(thing: () -> T): T
