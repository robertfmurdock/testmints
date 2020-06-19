package com.zegreatrob.testmints

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.ReporterProvider


interface StandardMintDispatcher : ReporterProvider {

// Vernacular based on http://xunitpatterns.com/Four%20Phase%20Test.html

    fun <C : Any> setup(context: C, additionalSetupActions: C.() -> Unit = {}) = Setup(
            context, reporter, additionalSetupActions, {}
    )

    fun <SC : Any> testTemplate(sharedSetup: () -> SC, sharedTeardown: (SC) -> Unit) = TestTemplate(
            sharedSetup, sharedTeardown, reporter
    )

    fun testTemplate(wrapper: (() -> Unit) -> Unit): TestTemplate<Unit> = TestTemplate(
            { }, { }, reporter, wrapper
    )

}

class TestTemplate<SC : Any>(
        private val templateSetup: () -> SC,
        private val templateTeardown: (SC) -> Unit,
        private val reporter: MintReporter,
        private val wrapper: (() -> Unit) -> Unit = { it() }
) {
    operator fun <C : Any> invoke(context: C, additionalSetupActions: C.() -> Unit = {}) =
            Setup(context, reporter, additionalSetupActions, templateSetup, templateTeardown, wrapper)

    fun extend(sharedSetup: () -> Unit = {}, sharedTeardown: () -> Unit = {}) = TestTemplate(
            templateSetup = { templateSetup().also { sharedSetup() } },
            templateTeardown = { sharedTeardown(); templateTeardown(it) },
            reporter = reporter,
            wrapper = wrapper
    )
}

class Setup<C : Any, SC : Any>(
        private val context: C,
        private val reporter: MintReporter,
        private val additionalSetupActions: C.() -> Unit,
        private val templateSetup: () -> SC,
        private val templateTeardown: (SC) -> Unit = {},
        private val wrapper: (() -> Unit) -> Unit = { it() }
) {
    infix fun <R> exercise(codeUnderTest: C.() -> R) = Exercise(context, reporter, templateTeardown, wrapper, {
        produceResult(codeUnderTest)
                .also { reporter.exerciseFinish() }
    })

    private fun <R> produceResult(codeUnderTest: C.() -> R): Pair<SC, R> {
        val setupContext = templateSetup()
        additionalSetupActions(context)
        reporter.exerciseStart(context)
        val result = codeUnderTest(context)
        return Pair(setupContext, result)
    }
}

class Exercise<C, R, SC>(
        private val context: C,
        private val reporter: MintReporter,
        private val templateTeardown: (SC) -> Unit,
        private val wrapper: (() -> Unit) -> Unit,
        private val exerciseFunc: () -> Pair<SC, R>
) {
    infix fun <R2> verify(assertionFunctions: C.(R) -> R2) = runTest(assertionFunctions)() {}

    private fun <R2> doVerify(assertionFunctions: C.(R) -> R2, result: R) = context
            .also { reporter.verifyStart(result) }
            .let { captureException { it.assertionFunctions(result) } }
            .also { reporter.verifyFinish() }

    infix fun <R2> verifyAnd(assertionFunctions: C.(R) -> R2) = Verify(runTest(assertionFunctions))

    private fun <R2> runTest(assertionFunctions: C.(R) -> R2): (C.(R) -> Unit) -> Unit = { teardownFunctions ->
        checkedInvoke(wrapper) {
            val (sharedContext, result) = exerciseFunc()
            val failure = doVerify(assertionFunctions, result)

            context.also { reporter.teardownStart() }
                    .run { captureException { teardownFunctions(result) } }
                    .let { it to captureException { templateTeardown(sharedContext) } }
                    .also { reporter.teardownFinish() }
                    .let { handleTeardownExceptions(it, failure) }
        }
    }

    private fun handleTeardownExceptions(pair: Pair<Throwable?, Throwable?>, failure: Throwable?) {
        val (teardownException, templateTeardownException) = pair
        val problems = exceptionDescriptionMap(teardownException, templateTeardownException, failure)

        if (problems.size == 1) {
            throw problems.values.first()
        } else if (problems.isNotEmpty()) {
            throw CompoundMintTestException(problems)
        }
    }

    private fun exceptionDescriptionMap(teardownException: Throwable?, templateTeardownException: Throwable?, failure: Throwable?) =
            mapOf(
                    "Failure" to failure,
                    "Teardown exception" to teardownException,
                    "Template teardown exception" to templateTeardownException
            )
                    .mapNotNull { (descriptor, exception) -> exception?.let { descriptor to exception } }
                    .toMap()
}

private fun checkedInvoke(wrapper: (() -> Unit) -> Unit, test: () -> Unit) {
    var testWasInvoked = false
    wrapper.invoke {
        testWasInvoked = true
        test()
    }
    if (!testWasInvoked) throw Exception("Incomplete test template: the wrapper function never called the test function")
}

class Verify<C, R>(private val runTest: (C.(R) -> Unit) -> Unit) {
    infix fun teardown(teardownFunctions: C.(R) -> Unit) = runTest(teardownFunctions)
}

