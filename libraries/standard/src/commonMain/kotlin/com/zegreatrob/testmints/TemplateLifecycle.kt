package com.zegreatrob.testmints

internal fun <SC : Any> runBuiltInSharedTemplate(
    sharedSetup: () -> SC,
    sharedTeardown: (SC) -> Unit,
    borrowedContext: Any? = null,
    cachedContext: Any? = null,
    test: TestFunc<SC>,
) {
    val sharedContext = sharedSetup()
    val testFailure = captureException { test(sharedContext) }
    val teardownFailure = captureException { sharedTeardown(sharedContext) }

    val closeFailure = if (sharedContext !== borrowedContext && sharedContext !== cachedContext && sharedContext is AutoCloseable) {
        captureException { sharedContext.close() }
    } else {
        null
    }

    val failures = mapOf(
        "Failure" to testFailure,
        "Template teardown exception" to teardownFailure,
        "Shared context close exception" to closeFailure,
    ).mapNotNull { (phase, failure) -> failure?.let { phase to it } }.toMap()

    when (failures.size) {
        0 -> Unit
        1 -> throw failures.values.first()
        else -> throw CompoundMintTestException(failures)
    }
}
