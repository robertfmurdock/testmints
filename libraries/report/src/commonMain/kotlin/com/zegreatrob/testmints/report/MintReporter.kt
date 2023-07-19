package com.zegreatrob.testmints.report

import kotlin.time.Duration

interface MintReporter {
    fun exerciseStart(context: Any) = Unit
    fun exerciseFinish() = Unit
    fun verifyStart(payload: Any?) = Unit
    fun verifyFinish(duration: Duration) = Unit
    fun teardownStart() = Unit
    fun teardownFinish() = Unit
}
