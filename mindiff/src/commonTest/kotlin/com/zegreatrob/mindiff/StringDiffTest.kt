package com.zegreatrob.mindiff

import com.zegreatrob.testmints.setup
import kotlin.test.Test
import kotlin.test.assertEquals

class StringDiffTest {

    @Test
    fun givenEqualWillReturnEmptyString() = setup(object {
        val l = "Hello world"
        val r = "Hello world"
    }) exercise {
        stringDiff(l, r)
    } verify { result ->
        assertEquals("", result)
    }

    class WhenStartsSameEndsDifferent {
        object Setup {
            const val l = "My man"
            const val r = "My lady"
        }

        @Test
        fun willIndicateWhereFirstDifferenceOccurs() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val t = result.split("\n")[0]
            assertEquals("Difference at index 3.", t)
        }

        @Test
        fun willShowDifferenceSection() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val expected = listOf(
                "E: man",
                "A: lady"
            )
            val takeLast = result.split("\n")
                .takeLast(2)
            assertEquals(expected, takeLast)
        }
    }

    class WhenMiddleSectionIsDifferent {
        object Setup {
            const val l = "The man dances well."
            const val r = "The lady dances well."
        }

        @Test
        fun willIndicateWhereFirstDifferenceOccurs() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val t = result.split("\n")[0]
            assertEquals("Difference at index 4.", t)
        }

        @Test
        fun willShowDifferenceSection() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val expected = listOf(
                "E: man",
                "A: lady"
            )
            val takeLast = result.split("\n")
                .takeLast(2)
            assertEquals(expected, takeLast)
        }
    }

    class WhenThereAreTwoDiscreetDifferences {
        object Setup {
            const val l = "The man dances well and is best at the jig."
            const val r = "The lady dances well and is best at the salsa."
        }

        @Test
        fun willIndicateFirstDifference() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val lines = result.split("\n")
            val t = lines[0]
            assertEquals("Difference at index 4.", t)
            val expected = listOf(
                "E: man",
                "A: lady"
            )
            val slice = lines.slice(1..2)
            assertEquals(expected, slice)
        }

        @Test
        fun willIndicateSecondDifference() = setup(Setup) exercise {
            stringDiff(l, r)
        } verify { result ->
            val lines = result.split("\n")
            val t = lines[3]
            assertEquals("Difference at index 39.", t)
            val expected = listOf(
                "E: jig",
                "A: salsa"
            )
            val slice = lines.slice(4..5)
            assertEquals(expected, slice)
        }
    }
}
