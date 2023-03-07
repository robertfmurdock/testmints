package com.zegreatrob.testmints.logs

import com.zegreatrob.testmints.setup
import kotlin.test.Test

class LoggingExampleTest {

    @Test
    fun manualLoggingTester() = setup(object {
    }) {
        println("setup func")
    } exercise {
        println("exercise func")
    } verify {
        println("verify func")
    }
}
