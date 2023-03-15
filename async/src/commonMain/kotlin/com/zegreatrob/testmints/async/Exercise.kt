package com.zegreatrob.testmints.async

import kotlinx.coroutines.Deferred

class Exercise<out C : Any, out R>(
    val timeoutMs: Long,
    private val runTestAsync: (suspend C.(R) -> Unit) -> (suspend C.(R?) -> Unit) -> Deferred<Unit>,
) {
    infix fun verify(assertionFunctions: suspend C.(R) -> Unit) = finalTransform(timeoutMs) {
        runTestAsync(assertionFunctions).invoke { }
    }
    infix fun verifyAnd(assertionFunctions: suspend C.(R) -> Unit) = Verify(timeoutMs, runTestAsync(assertionFunctions))
}
