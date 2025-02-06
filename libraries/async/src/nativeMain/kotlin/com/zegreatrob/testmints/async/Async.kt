package com.zegreatrob.testmints.async

import kotlinx.coroutines.runBlocking

actual suspend fun waitForTest(testFunction: () -> kotlinx.coroutines.test.TestResult) {
    testFunction()
}

actual fun <T> eventLoopProtect(thing: () -> T): T = runBlocking { thing() }
