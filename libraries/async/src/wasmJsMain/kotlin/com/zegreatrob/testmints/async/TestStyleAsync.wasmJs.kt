@file:OptIn(ExperimentalWasmJsInterop::class)

package com.zegreatrob.testmints.async

import kotlinx.coroutines.await
import kotlinx.coroutines.test.TestResult
import kotlin.js.Promise

actual suspend fun waitForTest(testFunction: () -> TestResult) {
    val testResult = testFunction()
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "CAST_NEVER_SUCCEEDS")
    (testResult as JsAny).unsafeCast<Promise<JsAny>>().await<JsAny?>()
}

actual fun <T> eventLoopProtect(thing: () -> T): T = thing()
