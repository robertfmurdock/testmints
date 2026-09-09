package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException

internal suspend fun <SC : Any> runBuiltInSharedTemplate(
    sharedSetup: suspend () -> SC,
    sharedTeardown: suspend (SC) -> Unit,
    test: TestFunc<SC>,
) {
    val sharedContext = sharedSetup()
    var testFailure: Throwable? = null
    try {
        test(sharedContext)
    } catch (failure: Throwable) {
        testFailure = failure
    }
    var teardownFailure: Throwable? = null
    try {
        sharedTeardown(sharedContext)
    } catch (failure: Throwable) {
        teardownFailure = failure
    }

    when {
        testFailure != null && teardownFailure != null -> throw CompoundMintTestException(
            mapOf(
                "Failure" to testFailure,
                "Template teardown exception" to teardownFailure,
            ),
        )

        testFailure != null -> throw testFailure

        teardownFailure != null -> throw teardownFailure
    }
}
