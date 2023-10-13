package com.zegreatrob.testmints

class Exercise<C, R>(private val runTest: (C.(R) -> Any) -> (C.(R) -> Unit) -> Unit) {
    @Suppress("ktlint:standard:unnecessary-parentheses-before-trailing-lambda")
    infix fun verify(assertionFunctions: C.(R) -> Unit) = runTest(assertionFunctions)() {}
    infix fun verifyAnd(assertionFunctions: C.(R) -> Unit) = Verify(runTest(assertionFunctions))
}
