package com.zegreatrob.mindiff

import com.zegreatrob.testmints.setup
import kotlin.test.Test
import kotlin.test.assertEquals

class DiffTest {

    @Test
    fun matchingCharactersAreMarkedWithADot() = setup(object {
        val l = "1"
        val r = "1"
    }) exercise {
        diff(l, r)
    } verify { result ->
        assertEquals(".", result)
    }

    @Test
    fun mismatchedCharactersAreMarkedWithAnX() = setup(object {
        val l = "1"
        val r = "0"
    }) exercise {
        diff(l, r)
    } verify { result ->
        assertEquals("x", result)
    }

    @Test
    fun marksTheSharedPrefixBeforeTheMismatches() = setup(object {
        val l = "I do that thing"
        val r = "I do the thing"
    }) exercise {
        diff(l, r)
    } verify { result ->
        assertEquals(".......xxxxxxxx", result)
    }

    @Test
    fun marksInterleavedMatchesAndMismatchesIncludingExtraCharacters() = setup(object {
        val l = "I do that thing"
        val r = "U dO Thot thang please"
    }) exercise {
        diff(l, r)
    } verify { result ->
        assertEquals("x..x.x.x....x..xxxxxxx", result)
    }
}
