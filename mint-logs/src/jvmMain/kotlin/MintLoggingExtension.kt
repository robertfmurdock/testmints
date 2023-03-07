package com.zegreatrob.testmints.logs

import mu.KotlinLogging
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext

class MintLoggingExtension : BeforeAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private val logger by lazy { KotlinLogging.logger("testmints") }

    override fun beforeAll(context: ExtensionContext?) = JsonLoggingTestMintsReporter.initialize()

    override fun beforeTestExecution(context: ExtensionContext?) {
        logger.info { mapOf("step" to "test", "state" to "start") }
        logger.info { mapOf("step" to "setup", "state" to "start") }
    }

    override fun afterTestExecution(context: ExtensionContext?) = logger.info {
        mapOf("step" to "test", "state" to "finish")
    }
}
