package com.zegreatrob.testmints.async

import kotlinx.coroutines.await

actual suspend fun waitForTest(testFunction: () -> kotlinx.coroutines.test.TestResult) {
    testFunction().await<Any>()
}

actual fun <T> eventLoopProtect(thing: () -> T): T = thing()
