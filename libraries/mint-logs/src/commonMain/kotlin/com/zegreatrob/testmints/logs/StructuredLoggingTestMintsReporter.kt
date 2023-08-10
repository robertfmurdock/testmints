package com.zegreatrob.testmints.logs

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import io.github.oshai.kotlinlogging.KotlinLogging

class StructuredLoggingTestMintsReporter private constructor() : MintReporter {

    private val logger by lazy { KotlinLogging.logger("testmints") }

    override fun exerciseStart(context: Any) {
        logger.atInfo { message = "setup-finish"; payload = mapOf("step" to "setup", "state" to "finish") }
        logger.atInfo { message = "exercise-start"; payload = mapOf("step" to "exercise", "state" to "start") }
    }

    override fun exerciseFinish() = logger.atInfo {
        message = "exercise-finish"
        payload = mapOf("step" to "exercise", "state" to "finish")
    }

    override fun verifyStart(payload: Any?) = logger.atInfo {
        message = "verify-start"
        this.payload = mapOf(
            "step" to "verify",
            "state" to "start",
            "payload" to payload.toString(),
        )
    }

    override fun verifyFinish() = logger.atInfo {
        message = "verify-finish"
        payload = mapOf("step" to "verify", "state" to "finish")
    }

    companion object {
        fun initialize() {
            platformDefaults()
            MintReporterConfig.reporter = StructuredLoggingTestMintsReporter()
        }
    }
}

expect fun platformDefaults()
