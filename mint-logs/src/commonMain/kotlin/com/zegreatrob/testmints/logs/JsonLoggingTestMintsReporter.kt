package com.zegreatrob.testmints.logs

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import mu.KotlinLogging

class JsonLoggingTestMintsReporter private constructor() : MintReporter {

    private val logger by lazy { KotlinLogging.logger("testmints") }

    override fun exerciseStart(context: Any) {
        logger.info { mapOf("step" to "setup", "state" to "finish") }
        logger.info { mapOf("step" to "exercise", "state" to "start") }
    }

    override fun exerciseFinish() = logger.info { mapOf("step" to "exercise", "state" to "finish") }

    override fun verifyStart(payload: Any?) = logger.info {
        mapOf(
            "step" to "verify",
            "state" to "start",
            "payload" to payload.toString()
        )
    }

    override fun verifyFinish() = logger.info { mapOf("step" to "verify", "state" to "finish") }

    companion object {
        fun initialize() {
            MintReporterConfig.reporter = JsonLoggingTestMintsReporter()
        }
    }
}
