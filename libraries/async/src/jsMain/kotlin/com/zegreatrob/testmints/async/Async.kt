package com.zegreatrob.testmints.async

import kotlinx.coroutines.await

actual suspend fun waitForTest(testFunction: () -> kotlinx.coroutines.test.TestResult) {
    testFunction().await()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()
