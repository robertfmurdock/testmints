package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.test.runTest

fun finalTransform(deferred: () -> Deferred<Unit>) = runTest { deferred().await() }

expect suspend fun waitForTest(testFunction: () -> Unit)

expect fun <T> eventLoopProtect(thing: () -> T): T
