package com.zegreatrob.testmints.logs

import com.zegreatrob.testmints.report.MintReporter
import com.zegreatrob.testmints.report.MintReporterConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration

class StructuredLoggingTestMintsReporter private constructor() : MintReporter {

    private val logger by lazy { KotlinLogging.logger("testmints") }

    override fun exerciseStart(context: Any) {
        logger.atInfo {
            this.message = "setup finish"
            this.payload =  mapOf("step" to "setup", "state" to "finish")
        }
        logger.atInfo {
            this.payload = mapOf("step" to "exercise", "state" to "start")
            this.message = "exercise start"
        }
    }

    override fun exerciseFinish() = logger.atInfo {
        this.message = "exercise finish"
        this.payload = mapOf("step" to "exercise", "state" to "finish")
    }

    override fun verifyStart(payload: Any?) = logger.atInfo {
        this.message = "verify start"
        this.payload = mapOf(
            "step" to "verify",
            "state" to "start",
            "payload" to payload.toString(),
        )
    }

    override fun verifyFinish(duration: Duration) = logger.atInfo {
        this.message = "verify finish"
        this.payload = mapOf(
            "step" to "verify",
            "state" to "finish",
            "duration" to "$duration",
            "durationMillis" to duration.inWholeMilliseconds,
        )
    }

    companion object {
        fun initialize() {
            MintReporterConfig.reporter = StructuredLoggingTestMintsReporter()
        }
    }
}
