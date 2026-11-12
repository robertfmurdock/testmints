package com.zegreatrob.testmints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class CloseableContextTest {
    @Test
    fun closesContextBetweenExplicitAndTemplateTeardown() = setup(object {
        val calls = mutableListOf<String>()
        val template = testTemplate(sharedSetup = { "shared" }, sharedTeardown = { _: String -> calls += "template teardown" })

        fun runMint() = template(
            AutoCloseable {
                calls += "close"
            },
        ) exercise {
            calls += "exercise"
        } verifyAnd {
            calls += "verify"
        } teardown {
            calls += "teardown"
        }
    }) exercise {
        runMint()
    } verify {
        assertEquals(listOf("exercise", "verify", "teardown", "close", "template teardown"), calls)
    }

    @Test
    fun closesContextWhenAdditionalSetupThrows() = setup(object {
        val setupFailure = IllegalStateException("setup failed")
        var closeCount = 0

        fun runMint() = setup(
            AutoCloseable {
                closeCount++
            },
        ) {
            throw setupFailure
        } exercise {
        } verify {
        }
    }) exercise {
        captureException { runMint() }
    } verify { failure ->
        assertSame(setupFailure, failure)
        assertEquals(1, closeCount)
    }

    @Test
    fun reportsCloseFailureAlongsideExerciseFailure() = setup(object {
        val exerciseFailure = IllegalStateException("exercise failed")
        val closeFailure = IllegalStateException("close failed")

        fun runMint() = setup(
            AutoCloseable {
                throw closeFailure
            },
        ) exercise {
            throw exerciseFailure
        } verify {
        }
    }) exercise {
        captureException { runMint() }
    } verify { failure ->
        assertEquals(
            mapOf("Template teardown exception" to exerciseFailure, "Context close exception" to closeFailure),
            assertIs<CompoundMintTestException>(failure).exceptions,
        )
    }

    @Test
    fun closesContextWhenExplicitTeardownThrows() = setup(object {
        val teardownFailure = IllegalStateException("teardown failed")
        var closeCount = 0

        fun runMint() = setup(
            AutoCloseable {
                closeCount++
            },
        ) exercise {
        } verifyAnd {
        } teardown {
            throw teardownFailure
        }
    }) exercise {
        captureException { runMint() }
    } verify { failure ->
        assertEquals(teardownFailure.message, failure?.message)
        assertEquals(1, closeCount)
    }

    @Test
    fun reportsSharedCloseFailureAlongsideSharedTeardownFailure() = setup(object {
        val teardownFailure = IllegalStateException("shared teardown failed")
        val closeFailure = IllegalStateException("shared close failed")
        val template = testTemplate(
            sharedSetup = { AutoCloseable { throw closeFailure } },
            sharedTeardown = { _: AutoCloseable -> throw teardownFailure },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        captureException { runMint() }
    } verify { failure ->
        assertEquals(
            mapOf("Template teardown exception" to teardownFailure, "Shared context close exception" to closeFailure),
            assertIs<CompoundMintTestException>(failure).exceptions,
        )
    }

    @Test
    fun closesFreshMergedContextAndKeepsCachedContextOpen() = setup(object {
        val calls = mutableListOf<String>()
        var nextId = 0
        val template = testTemplate(sharedSetup = { Unit }).extend(
            beforeAll = { AutoCloseable { calls += "cached close" } },
            mergeContext = { _, _ ->
                val id = ++nextId
                object : AutoCloseable {
                    val contextId = id

                    override fun close() {
                        calls += "close($contextId)"
                    }
                }
            },
        )

        fun runMint() = template() exercise {
            calls += "exercise($contextId)"
        } verify {
        }
    }) exercise {
        runMint()
        runMint()
    } verify {
        assertEquals(listOf("exercise(1)", "close(1)", "exercise(2)", "close(2)"), calls)
    }

    @Test
    fun leavesReusedMergeContextOpenUntilParentTeardown() = setup(object {
        val calls = mutableListOf<String>()
        val template = testTemplate(
            sharedSetup = { AutoCloseable { calls += "close" } },
            sharedTeardown = { _: AutoCloseable -> calls += "parent teardown" },
        ).extend(
            beforeAll = { Unit },
            mergeContext = { parent, _ -> parent },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        runMint()
    } verify {
        assertEquals(listOf("parent teardown", "close"), calls)
    }

    @Test
    fun customWrapperClosesItsContextWhenExerciseFails() = setup(object {
        val exerciseFailure = IllegalStateException("exercise failed")
        var closeCount = 0
        val template = testTemplate<AutoCloseable>(wrapper = { test ->
            AutoCloseable { closeCount++ }.use { context ->
                test(context)
            }
        })

        fun runMint() = template() exercise {
            throw exerciseFailure
        } verify {
        }
    }) exercise {
        captureException { runMint() }
    } verify { failure ->
        assertEquals(exerciseFailure.message, failure?.message)
        assertEquals(1, closeCount)
    }

    @Test
    fun keepsCachedContextOpenWhenUsedDirectly() = setup(object {
        var closeCount = 0
        val template = testTemplate(sharedSetup = { Unit }).extend(
            beforeAll = { AutoCloseable { closeCount++ } },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        runMint()
    } verify {
        assertEquals(0, closeCount)
    }

    @Test
    fun sharedContextClosesAfterTemplateTeardown() = setup(object {
        var closeCount = 0
        var closeCountAtTemplateTeardown: Int? = null
        val template = testTemplate(
            sharedSetup = { AutoCloseable { closeCount++ } },
            sharedTeardown = { _: AutoCloseable ->
                closeCountAtTemplateTeardown = closeCount
            },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        runMint()
    } verify {
        assertEquals(0, closeCountAtTemplateTeardown)
        assertEquals(1, closeCount)
    }

    @Test
    fun disposesResourceThroughCloseableContext() = setup(object {
        val resource = DisposableResource()

        fun runMint() = setup(
            object : AutoCloseable {
                val resourceUnderTest = resource

                override fun close() = resourceUnderTest.dispose()
            },
        ) exercise {
            resourceUnderTest.isDisposed
        } verify { disposedDuringExercise ->
            assertEquals(false, disposedDuringExercise)
        }
    }) exercise {
        runMint()
    } verify {
        assertEquals(true, resource.isDisposed)
    }

    private class DisposableResource {
        var isDisposed = false
            private set

        fun dispose() {
            isDisposed = true
        }
    }
}
