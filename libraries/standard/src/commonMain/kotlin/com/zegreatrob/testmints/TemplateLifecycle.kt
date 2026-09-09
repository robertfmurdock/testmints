package com.zegreatrob.testmints

internal fun <SC : Any> runBuiltInSharedTemplate(
    sharedSetup: () -> SC,
    sharedTeardown: (SC) -> Unit,
    test: TestFunc<SC>,
) {
    val sharedContext = sharedSetup()
    val testFailure = captureException { test(sharedContext) }
    val teardownFailure = captureException { sharedTeardown(sharedContext) }

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
