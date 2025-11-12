package com.zegreatrob.testmints.example.testballoon

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.async.AsyncMintDispatcher
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.eventLoopProtect
import com.zegreatrob.testmints.async.waitForTest
import com.zegreatrob.testmints.captureException
import com.zegreatrob.testmints.report.MintReporter
import de.infix.testBalloon.framework.core.testSuite
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.fail

enum class Steps {
    TemplateSetup,
    TemplateTeardown,
    Setup,
    Exercise,
    Verify,
    Teardown,
}

enum class Call {
    ExerciseStart,
    ExerciseFinish,
    VerifyStart,
    VerifyFinish,
    TeardownStart,
    TeardownFinish,
}

@Suppress("unused")
val AsyncMintsTest by testSuite {
    testSuite("ExampleUsage") {
        fun Int.plusOne() = this + 1

        test("willOrganizeTestNicely") {
            asyncSetup(object {
                val input: Int = Random.nextInt()
                val expected = input + 1
            }) exercise {
                input.plusOne()
            } verify { result ->
                assertEquals(expected, result)
            }
        }

        test("suspendFunctionsCanBeUsedInSetupClosure") {
            asyncSetup(object {
                val input: Int = Random.nextInt()
                val expected = input + 1
                var databaseSetupCounter = 0
            }) {
                withContext(Dispatchers.Default) {
                    delay(4)
                    databaseSetupCounter++
                }
            } exercise {
                input + databaseSetupCounter
            } verify { result ->
                assertEquals(expected, result)
            }
        }
    }

    testSuite("Features") {
        test("verifyWillCaptureFailures") {
            asyncSetup(object {
                fun testThatFails() = asyncSetup() exercise {
                } verify { fail("LOL") }
            }) exercise {
                captureException {
                    waitForTest { testThatFails() }
                }
            } verify { result ->
                assertEquals("LOL", result?.message)
            }
        }
        test("canFailAsyncWithCoroutine") {
            asyncSetup(object {
                fun testThatFailsWithCoroutine() = asyncSetup().exercise {
                } verify {
                    withContext(Dispatchers.Default) {
                        delay(3)
                        fail("LOL")
                    }
                }
            }) exercise {
                captureException { waitForTest { testThatFailsWithCoroutine() } }
            } verify { result ->
                assertEquals("LOL", result?.message)
            }
        }

        test("verifyShouldThrowErrorWhenFailureOccurs") {
            asyncSetup(object {
                fun failingTest() = asyncSetup().exercise {}.verify { fail("LOL") }
            }) exercise {
                captureException { waitForTest { failingTest() } }
            } verify { result ->
                assertEquals("LOL", result?.message)
            }
        }

        test("exerciseShouldNotBeCalledUntilSetupIsComplete") {
            asyncSetup(object {
                val expectedValue: Int = Random.nextInt()
                var actualValue: Int? = null
                fun testThatSharesScopeExample() = asyncSetup(object {
                    val value = expectedValue
                }) exercise {
                    actualValue = value
                } verify {}
            }) exercise {
                waitForTest { testThatSharesScopeExample() }
            } verify {
                assertEquals(expectedValue, actualValue)
            }
        }
        test("verifyShouldNotBeCalledUntilAfterExerciseIsComplete") {
            asyncSetup(object {
                val expectedValue = Random.nextInt()
                var actualValue: Int? = null
                fun testThatForwardsResultOfExerciseToVerify() = asyncSetup() exercise {
                    expectedValue
                } verify { result ->
                    actualValue = result
                }
            }) exercise {
                waitForTest { testThatForwardsResultOfExerciseToVerify() }
            } verify {
                assertEquals(expectedValue, actualValue)
            }
        }

        test("verifyShouldHaveAccessToScopeOfSetupObject") {
            asyncSetup(object {
                val expectedValue = Random.nextInt()
                var actualValue: Int? = null
                fun testThatUsesSetupObjectInVerify() = asyncSetup(object {
                    val value = expectedValue
                }) exercise {
                } verify {
                    actualValue = value
                }
            }) exercise {
                waitForTest { testThatUsesSetupObjectInVerify() }
            } verify {
                assertEquals(expectedValue, actualValue)
            }
        }

        test("failuresThrownDuringVerifyWillFailTheTest") {
            asyncSetup(object {
                val assertionError = AssertionError("ExpectedAssertion ${Random.nextInt()}")
                fun testThatFailsDuringVerify(assertionError: AssertionError) = asyncSetup().exercise {
                } verify { throw assertionError }
            }) exercise {
                captureException { waitForTest { testThatFailsDuringVerify(assertionError) } }
            } verify { result ->
                assertEquals(assertionError.message, result?.message)
            }
        }
        test("failuresThrownDuringExerciseWillFailTheTest") {
            asyncSetup(object {
                val assertionError = AssertionError("ExpectedAssertion ${Random.nextInt()}")

                fun testThatFailsDuringExercise() = asyncSetup() exercise {
                    throw assertionError
                } verify {}
            }) exercise {
                captureException { waitForTest { testThatFailsDuringExercise() } }
            } verify { result ->
                assertEquals(assertionError.message, result?.message)
            }
        }
        test("canUseDeferredDuringExerciseClosure") {
            asyncSetup(object {
                val expected = Random.nextInt()
                val asyncGuy = object {
                    fun doThingAsync() = CompletableDeferred(expected)
                }
            }) exercise {
                asyncGuy.doThingAsync().await()
            } verify { result: Int ->
                assertEquals(expected, result)
            }
        }

        test("canUseSuspendFunctionsDuringExerciseClosure") {
            asyncSetup(object {
                val expected = Random.nextInt()
                val asyncGuy = object {
                    suspend fun doThingAsync() = CompletableDeferred(expected).await()
                }
            }) exercise {
                asyncGuy.doThingAsync()
            } verify { result: Int ->
                assertEquals(expected, result)
            }
        }

        test("whenExerciseReturnsLegalNullWillStillRunVerify") {
            asyncSetup(object {
                var verifyWasCalled = false
                fun testThatReturnsNull() = asyncSetup() exercise {
                    null
                } verify {
                    verifyWasCalled = true
                }
            }) exercise {
                waitForTest { testThatReturnsNull() }
            } verify {
                assertEquals(true, verifyWasCalled)
            }
        }

        test("canUseDeferredDuringVerifyClosure") {
            asyncSetup(object {
                val expected = Random.nextInt()
            }) exercise {
                object {
                    fun doThingAsync() = CompletableDeferred(expected)
                }
            } verify { asyncGuy ->
                val result = asyncGuy.doThingAsync().await()
                assertEquals(expected, result)
            }
        }

        test("canUseSuspendFunctionsDuringVerifyClosure") {
            asyncSetup(object {
                val expected = Random.nextInt()
            }) exercise {
                object {
                    suspend fun doThingAsync() = CompletableDeferred(expected).await()
                }
            } verify { asyncGuy ->
                val result = asyncGuy.doThingAsync()
                assertEquals(expected, result)
            }
        }

        test("setupObjectCanBeCreatedInSuspendClosure") {
            asyncSetup.with({
                val asyncProducedValue = CompletableDeferred(Random.nextInt()).await()
                object {
                    val asyncProducedValue = asyncProducedValue
                }
            }) {} exercise {
                asyncProducedValue
            } verify { result ->
                assertEquals(asyncProducedValue, result)
            }
        }

        test("setupCanContinueInSuspendableClosureBeforeExercise") {
            asyncSetup(object {
                val coolString = "${Random.nextDouble()}"
                var list = mutableListOf<String>()
            }) {
                val asyncProducedValue = CompletableDeferred("$coolString And ${Random.nextInt()}").await()
                list.add(asyncProducedValue)
            } exercise {
                list.apply { shuffle() }
            } verify { result ->
                assertEquals(1, result.size)
            }
        }

        fun nativeEventLoopWeirdnessProtection(thing: () -> Unit) = eventLoopProtect(thing)

        test("usingScopeMintWillProvideSetupScopeThatWillCompleteBeforeExercise") {
            nativeEventLoopWeirdnessProtection {
                asyncSetup(object : ScopeMint() {
                    val expectedValue = Random.nextInt()
                    val asyncProducedValue = setupScope.async {
                        delay(40)
                        expectedValue
                    }
                }) exercise {
                    asyncProducedValue.isCompleted
                } verify { setupAsyncCompletedBeforeExercise ->
                    assertEquals(true, setupAsyncCompletedBeforeExercise)
                    assertEquals(expectedValue, asyncProducedValue.await())
                }
            }
        }
        test("usingScopeMintWillProvideExerciseScopeThatWillCompleteBeforeVerify") {
            asyncSetup(object : ScopeMint() {
                val expectedValue = Random.nextInt()
            }) exercise {
                exerciseScope.async {
                    delay(40)
                    expectedValue
                }
            } verify { result ->
                assertEquals(true, result.isCompleted)
                assertEquals(expectedValue, result.await())
            }
        }

        test("canMakeScopeInExerciseThatWillCompleteBeforeVerify") {
            asyncSetup(object : ScopeMint() {
                val expectedValue = Random.nextInt()
            }) exercise {
                coroutineScope {
                    async {
                        delay(40)
                        expectedValue
                    }
                }
            } verify { result ->
                assertEquals(true, result.isCompleted)
                assertEquals(expectedValue, result.await())
            }
        }

        test("tearDownShouldHaveAccessToScopeOfSetupObjectAndResult") {
            asyncSetup(object {
                val expectedValue: Int = Random.nextInt()
                val expectedResult: Int = Random.nextInt()
                val valueCollector = mutableListOf<Pair<Int, Int?>>()
            }) exercise {
                fun testThatSendsContextToTeardown() = asyncSetup(object {
                    val value = expectedValue
                }) exercise {
                    expectedResult
                } verifyAnd {
                } teardown { result ->
                    valueCollector.add(value to result)
                }

                waitForTest { testThatSendsContextToTeardown() }
            } verify {
                assertEquals(expectedValue to expectedResult, valueCollector[0])
            }
        }
        test("whenFailureOccursInVerifyAndExceptionOccursInTeardownBothAreReported") {
            asyncSetup(object {
                val verifyFailure = AssertionError("Got 'em")
                val teardownException = Exception("Oh man, not good. ${Random.nextInt()}")

                fun failingTestThatExplodesInTeardown() = asyncSetup() exercise {
                } verifyAnd { throw verifyFailure } teardown { throw teardownException }
            }) exercise {
                captureException { waitForTest { failingTestThatExplodesInTeardown() } }
            } verify { result ->
                when (result) {
                    is CompoundMintTestException -> {
                        assertEquals(verifyFailure.message, result.exceptions["Failure"]?.message)
                        assertEquals(teardownException.message, result.exceptions["Teardown exception"]?.message)
                    }

                    else -> fail("was not correct exception type.")
                }
            }
        }

        test("whenExceptionOccursInExerciseAndExceptionOccursInTeardownBothAreReported") {
            asyncSetup(object {
                val exerciseFailure = AssertionError("Got 'em")
                val teardownException = Exception("Oh man, not good. ${Random.nextInt()}")

                fun failingTestThatExplodesInTeardown() = asyncSetup() exercise {
                    throw exerciseFailure
                } verifyAnd { } teardown { throw teardownException }
            }) exercise {
                captureException { waitForTest { failingTestThatExplodesInTeardown() } }
            } verify { result ->
                when (result) {
                    is CompoundMintTestException -> {
                        assertEquals(exerciseFailure.message, result.exceptions["Exercise exception"]?.message)
                        assertEquals(teardownException.message, result.exceptions["Teardown exception"]?.message)
                    }

                    else -> fail("was not correct exception type.")
                }
            }
        }

        test("whenExceptionOccursInSetupClosureWillNotRunExerciseOrTeardown") {
            asyncSetup(object {
                val setupException = Exception("Oh man, not good. ${Random.nextInt()}")
                var exerciseOrVerifyTriggered = false

                fun testThatExplodeInSetupClosure() = asyncSetup {
                    throw setupException
                } exercise { exerciseOrVerifyTriggered = true } verify { exerciseOrVerifyTriggered = true }
            }) exercise {
                captureException { waitForTest { testThatExplodeInSetupClosure() } }
            } verify { result ->
                assertEquals(setupException.message, result?.message)
            }
        }
        testSuite("TestTemplates") {
            val correctOrder = listOf(
                Steps.TemplateSetup,
                Steps.Setup,
                Steps.Exercise,
                Steps.Verify,
                Steps.Teardown,
                Steps.TemplateTeardown,
            )

            test("whenTestSucceedsSharedSetupAndSharedTeardownRunInCorrectOrder") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add(Steps.TemplateSetup) },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) },
                    )

                    fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify) }
                        .teardown { calls.add(Steps.Teardown) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder, calls)
                }
            }
            test("whenTestSucceedsEndingWithVerifySharedSetupAndSharedTeardownRunInCorrectOrder") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add(Steps.TemplateSetup) },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) },
                    )

                    fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verify { calls.add(Steps.Verify) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder - Steps.Teardown, calls)
                }
            }

            test("testTemplateCanBeExtendedByCallingTestTemplateAgain") {
                asyncSetup(object {
                    fun test() = bolsteredCustomSetup() exercise {} verify {}
                    val calls = mutableListOf<String>()
                    val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add("setup") },
                        sharedTeardown = { calls.add("teardown") },
                    )
                    val bolsteredCustomSetup = customSetup.extend(
                        sharedSetup = { calls.add("inner setup") },
                        sharedTeardown = { calls.add("inner teardown") },
                    )
                }) exercise {
                    waitForTest { test() }
                } verify {
                    assertEquals(listOf("setup", "inner setup", "inner teardown", "teardown"), calls)
                }
            }
            test("wrapperFunctionCanBeUsedAsAlternativeToSharedSetupAndSharedTeardown") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    fun templateSetup() = calls.add(Steps.TemplateSetup)
                    fun templateTeardown() = calls.add(Steps.TemplateTeardown)
                    val customSetup = asyncTestTemplate(wrapper = { runTest ->
                        templateSetup()
                        runTest()
                        templateTeardown()
                    })

                    fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verify { calls.add(Steps.Verify) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder - Steps.Teardown, calls)
                }
            }
            test("wrapperFunctionProvideSharedContext") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val expectedSharedContext = 17
                    val customSetup = asyncTestTemplate(wrapper = { runTest: suspend (sharedContext: Int) -> Unit ->
                        calls.add(Steps.TemplateSetup)
                        runTest(expectedSharedContext)
                        calls.add(Steps.TemplateTeardown)
                    })
                    var sharedContextReceived = 0
                    fun testThatSucceeds() = customSetup.with({ sc ->
                        object {}.also {
                            sharedContextReceived = sc
                        }
                    }) { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verify { calls.add(Steps.Verify) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder - Steps.Teardown, calls)
                    assertEquals(expectedSharedContext, sharedContextReceived)
                }
            }
            test("templateSharedContextCanBeUsedAsContext") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val sharedContext = 17
                    val customSetup = asyncTestTemplate(wrapper = { runTest: suspend (sharedContext: Int) -> Unit ->
                        calls.add(Steps.TemplateSetup)
                        runTest(sharedContext)
                        calls.add(Steps.TemplateTeardown)
                    })
                    var contextReceived: Int? = null
                    fun testThatSucceeds() = customSetup { calls.add(Steps.Setup) }
                        .exercise {
                            contextReceived = this
                            calls.add(Steps.Exercise)
                        }
                        .verify { calls.add(Steps.Verify) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder - Steps.Teardown, calls)
                    assertEquals(sharedContext, contextReceived)
                }
            }
            test("canExtendToTransformSharedContextUsingWrapper") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val originalSharedContext = 67
                    val extendedSetup = asyncTestTemplate(sharedSetup = { originalSharedContext })
                        .extend(wrapper = { sc, test ->
                            calls.add(Steps.TemplateSetup)
                            test("$sc bottles of beer on the wall.")
                            calls.add(Steps.TemplateTeardown)
                        })
                    var sharedContextReceived: Any? = null
                    fun testThatSucceeds() = extendedSetup.with({ sc -> sharedContextReceived = sc }) {
                        calls.add(Steps.Setup)
                    }.exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify) }
                        .teardown { calls.add(Steps.Teardown) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder, calls)
                    assertEquals("$originalSharedContext bottles of beer on the wall.", sharedContextReceived)
                }
            }
            test("canExtendToTransformSharedContextUsingSharedSetupAndTeardown") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val originalSharedContext = 41
                    val extendedSetup = asyncTestTemplate(sharedSetup = { originalSharedContext })
                        .extend(
                            sharedSetup = { sc ->
                                calls.add(Steps.TemplateSetup)
                                "$sc bottles of beer on the wall."
                            },
                            sharedTeardown = { calls.add(Steps.TemplateTeardown) },
                        )
                    var sharedContextReceived: Any? = null
                    fun testThatSucceeds() = extendedSetup.with({ sc -> sharedContextReceived = sc }) {
                        calls.add(Steps.Setup)
                    }.exercise { calls.add(Steps.Exercise) }
                        .verifyAnd { calls.add(Steps.Verify) }
                        .teardown { calls.add(Steps.Teardown) }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(correctOrder, calls)
                    assertEquals("$originalSharedContext bottles of beer on the wall.", sharedContextReceived)
                }
            }
            test("whenWrapperFunctionDoesNotCallTheTestTheTestWillFail") {
                asyncSetup(object {
                    val customSetup = asyncTestTemplate(wrapper = {})
                    fun testThatFailsBecauseOfBadTemplate() = customSetup()
                        .exercise { }
                        .verify { }
                }) exercise {
                    captureException { waitForTest { testThatFailsBecauseOfBadTemplate() } }
                } verify { result ->
                    assertEquals(
                        "Incomplete test template: the wrapper function never called the test function",
                        result?.message,
                    )
                }
            }
            test("whenWrapperFunctionDoesNotCallTheTestTheTestWillFailIncludingTeardown") {
                asyncSetup(object {
                    val customSetup = asyncTestTemplate(wrapper = {})
                    fun testThatFailsBecauseOfBadTemplate() = customSetup()
                        .exercise { }
                        .verifyAnd { }
                        .teardown { }
                }) exercise {
                    captureException { waitForTest { testThatFailsBecauseOfBadTemplate() } }
                } verify { result ->
                    assertEquals(
                        "Incomplete test template: the wrapper function never called the test function",
                        result?.message,
                    )
                }
            }
            test("whenVerifyFailsSharedSetupAndSharedTeardownRunInCorrectOrder") {
                asyncSetup(object {
                    val calls = mutableListOf<Steps>()
                    val customSetup = asyncTestTemplate(
                        sharedSetup = { calls.add(Steps.TemplateSetup) },
                        sharedTeardown = { calls.add(Steps.TemplateTeardown) },
                    )

                    fun testThatFails() = customSetup { calls.add(Steps.Setup) }
                        .exercise { calls.add(Steps.Exercise) }
                        .verifyAnd {
                            calls.add(Steps.Verify)
                            fail("This test fails.")
                        }
                        .teardown { calls.add(Steps.Teardown) }
                }) exercise {
                    captureException { waitForTest { testThatFails() } }
                } verify {
                    assertEquals(correctOrder, calls)
                }
            }
            test("whenExceptionOccursInTeardownAndInTemplateTeardownBothAreReported") {
                asyncSetup(object {
                    val teardownException = Exception("Oh man, not good.")
                    val templateTeardownException = Exception("Now we're really off-road")
                    val customSetup = asyncTestTemplate(
                        sharedSetup = {},
                        sharedTeardown = { throw templateTeardownException },
                    )

                    fun failingTestThatExplodesInTeardown() = customSetup() exercise {} verifyAnd {
                    } teardown { throw teardownException }
                }) exercise {
                    captureException { waitForTest { failingTestThatExplodesInTeardown() } }
                } verify { result ->
                    val expected = CompoundMintTestException(
                        mapOf(
                            "Teardown exception" to teardownException,
                            "Template teardown exception" to templateTeardownException,
                        ),
                    )
                    assertEquals(expected, result)
                }
            }
            test("sharedSetupCanReturnContextThatWillBeProvidedToTheTeardown") {
                asyncSetup(object {
                    val int = Random.nextInt()
                    val callArguments = mutableListOf<Any>()
                    val customSetup = asyncTestTemplate(
                        sharedSetup = { int },
                        sharedTeardown = { it: Int -> callArguments.add(it) },
                    )

                    fun testThatSucceeds() = customSetup { }
                        .exercise { }
                        .verify { }
                }) exercise {
                    waitForTest { testThatSucceeds() }
                } verify {
                    assertEquals(listOf<Any>(int), callArguments)
                }
            }
            suspend fun List<() -> TestResult>.runSuite() = forEach { waitForTest { it() } }

            test("templateCanBeBuiltWithBeforeAllFunctionThatWillOnlyRunOnceForAllAttachedTests") {
                asyncSetup(object {
                    var beforeAllCount = 0
                    val customSetup = asyncTestTemplate(beforeAll = { beforeAllCount++ })
                    val testSuite = (1..3).map {
                        fun() = customSetup { }
                            .exercise { }
                            .verify { }
                    }
                }) exercise {
                    testSuite.runSuite()
                } verify {
                    assertEquals(1, beforeAllCount)
                }
            }
            test("templateCanBeExtendedWithBeforeAllFunctionThatWillOnlyRunOnceForAllAttachedTests") {
                asyncSetup(object {
                    var calls = mutableListOf<String>()
                    val customSetup = asyncTestTemplate(wrapper = {
                        calls.add("wrapSetup")
                        it()
                        calls.add("wrapTeardown")
                    }).extend(beforeAll = { calls.add("beforeAll") })
                    val testSuite = (1..3).map {
                        fun() = customSetup { }
                            .exercise { }
                            .verify { }
                    }
                }) exercise {
                    testSuite.runSuite()
                } verify {
                    assertEquals(
                        listOf(
                            "wrapSetup",
                            "beforeAll",
                            "wrapTeardown",
                            "wrapSetup",
                            "wrapTeardown",
                            "wrapSetup",
                            "wrapTeardown",
                        ),
                        calls,
                    )
                }
            }

            test("templateWithBeforeAllWillNotPerformBeforeAllWhenThereAreNoTests") {
                asyncSetup(object {
                    var beforeAllCount = 0
                    val customSetup = asyncTestTemplate(beforeAll = { beforeAllCount++ })
                    val testSuite: List<() -> TestResult> = emptyList()
                }) exercise {
                    testSuite.runSuite()
                } verify {
                    assertEquals(0, beforeAllCount)
                }
            }
            test("templateExtendedByBeforeAllCanMergeSharedContextEasily") {
                asyncSetup(object {
                    val parentSharedContext = "parent shared context"
                    val innerBeforeAllContext = 87

                    val customSetup = asyncTestTemplate<String>(wrapper = { it(parentSharedContext) })
                        .extend(beforeAll = { innerBeforeAllContext }, mergeContext = { sc, bac ->
                            Pair(sc, bac)
                        })

                    var capturedContext: Any? = null

                    fun theCoolTest() = customSetup()
                        .exercise { capturedContext = this }
                        .verify { }
                }) exercise {
                    waitForTest { theCoolTest() }
                } verify {
                    assertEquals(Pair(parentSharedContext, innerBeforeAllContext), capturedContext)
                }
            }
        }
        testSuite("ReporterFeatures") {
            test("willReportTestEventInOrderToReporter") {
                asyncSetup(object : AsyncMintDispatcher {
                    val calls = mutableListOf<Call>()
                    private fun record(call: Call) = calls.add(call).let { }

                    override val reporter = object : MintReporter {
                        override fun exerciseStart(context: Any) = record(Call.ExerciseStart)
                        override fun exerciseFinish() = record(Call.ExerciseFinish)
                        override fun verifyStart(payload: Any?) = record(Call.VerifyStart)
                        override fun verifyFinish() = record(Call.VerifyFinish)
                        override fun teardownStart() = record(Call.TeardownStart)
                        override fun teardownFinish() = record(Call.TeardownFinish)
                    }

                    fun exampleTest() = asyncSetup() exercise {} verifyAnd {} teardown {}
                }) exercise {
                    waitForTest { exampleTest() }
                } verify {
                    assertEquals(
                        expected = listOf(
                            Call.ExerciseStart,
                            Call.ExerciseFinish,
                            Call.VerifyStart,
                            Call.VerifyFinish,
                            Call.TeardownStart,
                            Call.TeardownFinish,
                        ),
                        actual = calls,
                    )
                }
            }
            test("reporterCanBeConfiguredAfterTemplatesAreDefined") {
                asyncSetup(object : AsyncMintDispatcher {
                    val templatedSetup = asyncTestTemplate(sharedSetup = {})

                    fun simpleTest() = templatedSetup() exercise {} verifyAnd {} teardown {}
                    var exerciseCalled = false
                    override val reporter = object : MintReporter {
                        override fun exerciseStart(context: Any) {
                            exerciseCalled = true
                        }
                    }
                }) exercise {
                    waitForTest { simpleTest() }
                } verify {
                    assertEquals(true, exerciseCalled)
                }
            }

            test("exerciseStartWillLogContext") {
                asyncSetup(object : AsyncMintDispatcher {
                    var exerciseStartPayload: Any? = null
                    override val reporter = object : MintReporter {
                        override fun exerciseStart(context: Any) {
                            exerciseStartPayload = context
                        }
                    }
                    val expectedObject = object {}

                    fun simpleTest() = asyncSetup(expectedObject) exercise { } verify {}
                }) exercise {
                    waitForTest { simpleTest() }
                } verify {
                    assertEquals(expectedObject, exerciseStartPayload)
                }
            }
            test("verifyStartWillLogThePayload") {
                asyncSetup(object : AsyncMintDispatcher {
                    var verifyStartPayload: Any? = null

                    override val reporter = object : MintReporter {
                        override fun verifyStart(payload: Any?) {
                            verifyStartPayload = payload
                        }
                    }
                    val expectedResult = object {}
                    fun simpleTest() = asyncSetup() exercise { expectedResult } verify {}
                }) exercise {
                    waitForTest { simpleTest() }
                } verify {
                    assertEquals(expectedResult, verifyStartPayload)
                }
            }
            test("verifyFinishWillWaitUntilVerifyIsComplete") {
                asyncSetup(object : AsyncMintDispatcher {
                    val verifyState = mutableListOf<String>()
                    var result: String? = null
                    override val reporter = object : MintReporter {
                        override fun verifyFinish() {
                            result = verifyState.joinToString("")
                        }
                    }
                    val expectedResult = object {}
                    val expectedException = Exception("end in failure")

                    fun simpleTest() = asyncSetup() exercise { expectedResult } verify {
                        verifyState.add("a")
                        delay(20)
                        verifyState.add("b")
                        delay(20)
                        verifyState.add("c")
                        throw expectedException
                    }
                }) exercise {
                    captureException { waitForTest { simpleTest() } }
                } verify { exception ->
                    assertEquals(expectedException.message, exception?.message)
                    assertEquals("abc", result)
                }
            }
        }
    }
}
