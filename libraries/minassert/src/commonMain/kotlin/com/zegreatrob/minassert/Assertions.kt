package com.zegreatrob.minassert

import com.zegreatrob.mindiff.stringDiff
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

fun <T> T?.assertIsEqualTo(expected: T, message: String? = null) = assertEquals(expected, this, message.appendDiff(expected, this))

private fun <T> String?.appendDiff(expected: T, t1: T?): String = "${this ?: ""}\n${stringDiff(expected.toString(), t1.toString())}\n"

fun <T> T?.assertIsNotEqualTo(expected: T, message: String? = null) {
    assertTrue("${if (message == null) "" else "$message. "}Two values were unexpectedly identical: $expected") {
        this != expected
    }
}

fun <T> List<T>?.assertContains(item: T): List<T>? {
    if (this == null) {
        fail("Target list was null")
    }
    assertTrue("${map { "$it" }} did not contain $item") { contains(item) }
    return this
}
