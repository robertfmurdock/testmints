package com.zegreatrob.testmints.async

actual suspend fun waitForTest(testFunction: () -> kotlinx.coroutines.test.TestResult) {
    testFunction()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()
