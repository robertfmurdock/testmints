package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel

class Setup<out C : Any, out SC : Any>(
    private val contextProvider: suspend (SC) -> C,
    private val scope: CoroutineScope,
    private val additionalActions: suspend C.() -> Unit,
    private val reporter: MintReporter,
    private val timeoutMs: Long,
    private val wrapper: suspend (TestFunc<SC>) -> Unit,
) {
    infix fun <R> exercise(exerciseFunc: suspend C.() -> R) = Exercise(timeoutMs) { verifyFunc ->
        { teardownFunc ->
            scope.async { runTest(exerciseFunc, verifyFunc, teardownFunc) }.apply {
                invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
            }
        }
    }

    private suspend fun <R> runTest(
        exerciseFunc: suspend C.() -> R,
        verifyFunc: suspend C.(R) -> Unit,
        teardownFunc: suspend C.(R?) -> Unit,
    ) {
        var exerciseProblem: Throwable? = null
        var verifyFailure: Throwable? = null
        var teardownException: Throwable? = null
        var closeException: Throwable? = null
        val wrapperException = checkedInvoke(wrapper) { sharedContext ->
            val context = contextProvider(sharedContext)
            try {
                performSetup(context)
                val result = try {
                    performExercise(context, exerciseFunc)
                        .let { Result.success(it) }
                } catch (uhOh: Throwable) {
                    exerciseProblem = uhOh
                    Result.failure(uhOh)
                }
                verifyFailure = if (result.isSuccess) performVerify(context, result.getOrThrow(), verifyFunc) else null

                teardownException = performTeardown(context, result, teardownFunc)
            } finally {
                if (context !== sharedContext && context is AutoCloseable) {
                    closeException = captureException { context.close() }
                }
            }
        }
        reporter.teardownFinish()
        handleTeardownExceptions(exerciseProblem, verifyFailure, teardownException, wrapperException, closeException)
    }

    private suspend fun <R> performTeardown(context: C, result: Result<R>, teardownFunc: suspend C.(R?) -> Unit): Throwable? {
        reporter.teardownStart()
        return captureException { teardownFunc(context, result.getOrNull()) }
    }

    private suspend fun <R> performVerify(
        context: C,
        result: R,
        assertionFunctions: suspend C.(R) -> Unit,
    ): Throwable? {
        reporter.verifyStart(result)
        return captureException { context.assertionFunctions(result) }.also {
            reporter.verifyFinish()
        }
    }

    private suspend fun performSetup(context: C) {
        additionalActions(context)
        if (context is ScopeMint) {
            waitForJobsToFinish(context.setupScope)
        }
    }

    private suspend fun <R> performExercise(context: C, exerciseFunc: suspend C.() -> R): R {
        reporter.exerciseStart(context)
        val result = try {
            Result.success(exerciseFunc(context))
        } catch (failure: Throwable) {
            Result.failure(failure)
        }
        if (context is ScopeMint) {
            waitForJobsToFinish(context.exerciseScope)
        }
        reporter.exerciseFinish()
        return result.getOrThrow()
    }
}

private fun Throwable.wrapCause() = CancellationException("Test failure.", this)

private fun handleTeardownExceptions(
    exerciseProblem: Throwable?,
    failure: Throwable?,
    teardownException: Throwable?,
    templateTeardownException: Throwable?,
    closeException: Throwable?,
) {
    val problems = exceptionDescriptionMap(exerciseProblem, failure, teardownException, templateTeardownException, closeException)

    if (problems.size == 1) {
        throw problems.values.first()
    } else if (problems.isNotEmpty()) {
        throw CompoundMintTestException(problems)
    }
}

private fun exceptionDescriptionMap(
    exerciseProblem: Throwable?,
    failure: Throwable?,
    teardownException: Throwable?,
    templateTeardownException: Throwable?,
    closeException: Throwable?,
) = descriptionMap(exerciseProblem, failure, teardownException, templateTeardownException, closeException)
    .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
    .toMap()

private fun descriptionMap(
    exerciseProblem: Throwable?,
    failure: Throwable?,
    teardownException: Throwable?,
    templateTeardownException: Throwable?,
    closeException: Throwable?,
) = mapOf(
    "Exercise exception" to exerciseProblem,
    "Failure" to failure,
    "Teardown exception" to teardownException,
    "Template teardown exception" to templateTeardownException,
    "Context close exception" to closeException,
)

private suspend fun <SC : Any> checkedInvoke(
    wrapper: suspend (TestFunc<SC>) -> Unit,
    test: TestFunc<SC>,
) = captureException {
    var testWasInvoked = false
    wrapper.invoke { sharedContext ->
        testWasInvoked = true
        test(sharedContext)
    }
    if (!testWasInvoked) throw Exception("Incomplete test template: the wrapper function never called the test function")
}
