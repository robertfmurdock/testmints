package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.milliseconds

fun finalTransform(timeoutMs: Long, deferred: () -> Deferred<Unit>) = runTest(timeout = timeoutMs.milliseconds) {
    deferred().await()
}

expect suspend fun waitForTest(testFunction: () -> Unit)

expect fun <T> eventLoopProtect(thing: () -> T): T
