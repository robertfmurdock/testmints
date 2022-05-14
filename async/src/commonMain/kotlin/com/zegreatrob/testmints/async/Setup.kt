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
    private val wrapper: suspend (TestFunc<SC>) -> Unit
) {
    infix fun <R> exercise(exerciseFunc: suspend C.() -> R) = Exercise<C, R>(timeoutMs) { verifyFunc ->
        { teardownFunc ->
            scope.async { runTest(exerciseFunc, verifyFunc, teardownFunc) }.apply {
                invokeOnCompletion { cause -> scope.cancel(cause?.wrapCause()) }
            }
        }
    }

    private suspend fun <R> runTest(
        exerciseFunc: suspend C.() -> R,
        verifyFunc: suspend C.(R) -> Unit,
        teardownFunc: suspend C.(R?) -> Unit
    ) {
        var exerciseProblem: Throwable? = null
        var verifyFailure: Throwable? = null
        var teardownException: Throwable? = null
        val wrapperException = checkedInvoke(wrapper) { sharedContext ->
            val context = performSetup(sharedContext)
            val result = try {
                performExercise(context, exerciseFunc)
                    .let { Result.success(it) }
            } catch (uhOh: Throwable) {
                exerciseProblem = uhOh
                Result.failure(uhOh)
            }
            verifyFailure = if (result.isSuccess) performVerify(context, result.getOrThrow(), verifyFunc) else null

            teardownException = performTeardown(context, result, teardownFunc)
        }
        reporter.teardownFinish()
        handleTeardownExceptions(exerciseProblem, verifyFailure, teardownException, wrapperException)
    }

    private suspend fun <R> performTeardown(context: C, result: Result<R>, teardownFunc: suspend C.(R?) -> Unit):
        Throwable? {
        reporter.teardownStart()
        return captureException { teardownFunc(context, result.getOrNull()) }
    }

    private suspend fun <R> runCodeUnderTest(context: C, codeUnderTest: suspend C.() -> R): R {
        reporter.exerciseStart(context)
        val result = codeUnderTest(context)
        reporter.exerciseFinish()
        return result
    }

    private suspend fun <R> performVerify(
        context: C,
        result: R,
        assertionFunctions: suspend C.(R) -> Unit
    ): Throwable? {
        reporter.verifyStart(result)
        return captureException { context.assertionFunctions(result) }.also {
            reporter.verifyFinish()
        }
    }

    private suspend fun performSetup(sharedContext: SC): C {
        val context = contextProvider(sharedContext)
        additionalActions(context)
        if (context is ScopeMint) {
            waitForJobsToFinish(context.setupScope)
        }
        return context
    }

    private suspend fun <R> performExercise(context: C, exerciseFunc: suspend C.() -> R) =
        runCodeUnderTest(context, exerciseFunc)
            .also {
                if (context is ScopeMint) {
                    waitForJobsToFinish(context.exerciseScope)
                }
            }
}

private fun Throwable.wrapCause() = CancellationException("Test failure.", this)

private fun handleTeardownExceptions(
    exerciseProblem: Throwable?,
    failure: Throwable?,
    teardownException: Throwable?,
    templateTeardownException: Throwable?
) {
    val problems = exceptionDescriptionMap(exerciseProblem, failure, teardownException, templateTeardownException)

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
    templateTeardownException: Throwable?
) = descriptionMap(exerciseProblem, failure, teardownException, templateTeardownException)
    .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
    .toMap()

private fun descriptionMap(
    exerciseProblem: Throwable?,
    failure: Throwable?,
    teardownException: Throwable?,
    templateTeardownException: Throwable?
) = mapOf(
    "Exercise exception" to exerciseProblem,
    "Failure" to failure,
    "Teardown exception" to teardownException,
    "Template teardown exception" to templateTeardownException
)

private suspend fun <SC : Any> checkedInvoke(
    wrapper: suspend (TestFunc<SC>) -> Unit,
    test: TestFunc<SC>
) = captureException {
    var testWasInvoked = false
    wrapper.invoke { sharedContext ->
        testWasInvoked = true
        test(sharedContext)
    }
    if (!testWasInvoked) throw Exception("Incomplete test template: the wrapper function never called the test function")
}
