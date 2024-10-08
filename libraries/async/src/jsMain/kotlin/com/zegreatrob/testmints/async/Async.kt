package com.zegreatrob.testmints.async

import kotlinx.coroutines.await
import kotlin.js.Promise

actual suspend fun waitForTest(testFunction: () -> kotlinx.coroutines.test.TestResult) {
    testFunction().unsafeCast<Promise<Unit>>().await()
}

actual fun <T> eventLoopProtect(thing: () -> T) = thing()
