package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.CompoundMintTestException
import com.zegreatrob.testmints.captureException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CloseableContextTest {
    @Test
    fun closesContextBetweenExplicitAndTemplateTeardown() = asyncSetup(object {
        val calls = mutableListOf<String>()
        val template = asyncTestTemplate(
            sharedSetup = { "shared" },
            sharedTeardown = { _: String -> calls += "template teardown" },
        )

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
        waitForTest { runMint() }
    } verify {
        assertEquals(listOf("exercise", "verify", "teardown", "close", "template teardown"), calls)
    }

    @Test
    fun closesContextWhenAdditionalSetupThrows() = asyncSetup(object {
        val setupFailure = IllegalStateException("setup failed")
        var closeCount = 0

        fun runMint() = asyncSetup(
            AutoCloseable {
                closeCount++
            },
        ) {
            throw setupFailure
        } exercise {
        } verify {
        }
    }) exercise {
        captureException { waitForTest { runMint() } }
    } verify { failure ->
        assertEquals(setupFailure.message, failure?.message)
        assertEquals(1, closeCount)
    }

    @Test
    fun reportsCloseFailureAlongsideExerciseFailure() = asyncSetup(object {
        val exerciseFailure = IllegalStateException("exercise failed")
        val closeFailure = IllegalStateException("close failed")

        fun runMint() = asyncSetup(
            AutoCloseable {
                throw closeFailure
            },
        ) exercise {
            throw exerciseFailure
        } verify {
        }
    }) exercise {
        captureException { waitForTest { runMint() } }
    } verify { failure ->
        assertEquals(
            mapOf("Exercise exception" to exerciseFailure, "Context close exception" to closeFailure),
            assertIs<CompoundMintTestException>(failure).exceptions,
        )
    }

    @Test
    fun closesContextWhenExplicitTeardownThrows() = asyncSetup(object {
        val teardownFailure = IllegalStateException("teardown failed")
        var closeCount = 0

        fun runMint() = asyncSetup(
            AutoCloseable {
                closeCount++
            },
        ) exercise {
        } verifyAnd {
        } teardown {
            throw teardownFailure
        }
    }) exercise {
        captureException { waitForTest { runMint() } }
    } verify { failure ->
        assertEquals(teardownFailure.message, failure?.message)
        assertEquals(1, closeCount)
    }

    @Test
    fun reportsSharedCloseFailureAlongsideSharedTeardownFailure() = asyncSetup(object {
        val teardownFailure = IllegalStateException("shared teardown failed")
        val closeFailure = IllegalStateException("shared close failed")
        val template = asyncTestTemplate(
            sharedSetup = { AutoCloseable { throw closeFailure } },
            sharedTeardown = { _: AutoCloseable -> throw teardownFailure },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        captureException { waitForTest { runMint() } }
    } verify { failure ->
        assertEquals(
            mapOf("Template teardown exception" to teardownFailure, "Shared context close exception" to closeFailure),
            assertIs<CompoundMintTestException>(failure).exceptions,
        )
    }

    @Test
    fun closesFreshMergedContextAndKeepsCachedContextOpen() = asyncSetup(object {
        val calls = mutableListOf<String>()
        var nextId = 0
        val template = asyncTestTemplate(sharedSetup = { Unit }).extend(
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
        waitForTest { runMint() }
        waitForTest { runMint() }
    } verify {
        assertEquals(listOf("exercise(1)", "close(1)", "exercise(2)", "close(2)"), calls)
    }

    @Test
    fun leavesReusedMergeContextOpenUntilParentTeardown() = asyncSetup(object {
        val calls = mutableListOf<String>()
        val template = asyncTestTemplate(
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
        waitForTest { runMint() }
    } verify {
        assertEquals(listOf("parent teardown", "close"), calls)
    }

    @Test
    fun customWrapperClosesItsContextWhenExerciseFails() = asyncSetup(object {
        val exerciseFailure = IllegalStateException("exercise failed")
        var closeCount = 0
        val template = asyncTestTemplate<AutoCloseable>(wrapper = { test ->
            AutoCloseable { closeCount++ }.use { context ->
                test(context)
            }
        })

        fun runMint() = template() exercise {
            throw exerciseFailure
        } verify {
        }
    }) exercise {
        captureException { waitForTest { runMint() } }
    } verify { failure ->
        assertEquals(exerciseFailure.message, failure?.message)
        assertEquals(1, closeCount)
    }

    @Test
    fun keepsCachedContextOpenWhenUsedDirectly() = asyncSetup(object {
        var closeCount = 0
        val template = asyncTestTemplate(sharedSetup = { Unit }).extend(
            beforeAll = { AutoCloseable { closeCount++ } },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        waitForTest { runMint() }
    } verify {
        assertEquals(0, closeCount)
    }

    @Test
    fun sharedContextClosesAfterTemplateTeardown() = asyncSetup(object {
        var closeCount = 0
        var closeCountAtTemplateTeardown: Int? = null
        val template = asyncTestTemplate(
            sharedSetup = { AutoCloseable { closeCount++ } },
            sharedTeardown = { _: AutoCloseable ->
                closeCountAtTemplateTeardown = closeCount
            },
        )

        fun runMint() = template() exercise {
        } verify {
        }
    }) exercise {
        waitForTest { runMint() }
    } verify {
        assertEquals(0, closeCountAtTemplateTeardown)
        assertEquals(1, closeCount)
    }
}
