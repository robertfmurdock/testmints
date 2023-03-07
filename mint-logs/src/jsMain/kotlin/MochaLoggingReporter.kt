import com.zegreatrob.testmints.logs.JsonLoggingTestMintsReporter
import mu.KotlinLogging

@JsName("MochaLoggingReporter")
@ExperimentalJsExport
@JsExport
object MochaLoggingReporter {
    private val logger by lazy { KotlinLogging.logger("testmints") }

    fun beforeAll() = JsonLoggingTestMintsReporter.initialize()

    fun beforeEach() = logger.info { mapOf("step" to "setup", "state" to "start") }
}
