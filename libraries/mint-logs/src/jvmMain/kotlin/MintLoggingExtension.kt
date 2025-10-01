package com.zegreatrob.testmints.logs

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.jvm.optionals.getOrNull

class MintLoggingExtension :
    BeforeAllCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback {

    private val logger by lazy { KotlinLogging.logger("testmints") }

    override fun beforeAll(context: ExtensionContext) = StructuredLoggingTestMintsReporter.initialize()

    override fun beforeTestExecution(context: ExtensionContext) {
        logger.atInfo {
            message = "test-start"
            payload = mapOf(
                "step" to "test",
                "state" to "start",
            )
        }
        logger.atInfo {
            message = "setup-start"
            payload = mapOf("step" to "setup", "state" to "start", "name" to context.testName())
        }
    }

    private fun ExtensionContext?.testName() = "${this?.parent?.getOrNull()?.displayName}.${this?.displayName}"

    override fun afterTestExecution(context: ExtensionContext) = logger.atInfo {
        message = "test-finish"
        payload = mapOf("step" to "test", "state" to "finish")
    }
}
