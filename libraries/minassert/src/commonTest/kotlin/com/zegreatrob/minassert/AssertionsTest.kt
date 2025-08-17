package com.zegreatrob.minassert

import com.zegreatrob.testmints.setup
import kotlin.test.Test

class AssertionsTest {

    @Test
    fun onFailureContainsMessageIsCorrect() = setup(object {
        val actual = listOf("A", "B", "C")
        val expected = "Z"
    }) exercise {
        runCatching { actual.assertContains(expected) }
    } verify { result ->
        result.exceptionOrNull()
            ?.message
            .assertIsEqualTo("[A, B, C] did not contain Z")
    }

    @Test
    fun onFailureSupportsNulls() = setup(object {
        val actual = null
        val expected = "Z"
    }) exercise {
        runCatching { actual.assertContains(expected) }
    } verify { result ->
        result.exceptionOrNull()
            ?.message
            .assertIsEqualTo("Target list was null")
    }

    @Test
    fun assertNotEqualWorks() = setup(object {
        val thing = "a"
        val sameThing = "a"
    }) exercise {
        runCatching { thing.assertIsNotEqualTo(sameThing) }
    } verify { result ->
        result.exceptionOrNull()
            ?.message
            .assertIsEqualTo("Two values were unexpectedly identical: a")
    }
}
