package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException

internal suspend fun <SC : Any> runBuiltInSharedTemplate(
    sharedSetup: suspend () -> SC,
    sharedTeardown: suspend (SC) -> Unit,
    borrowedContext: Any? = null,
    cachedContext: Any? = null,
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
