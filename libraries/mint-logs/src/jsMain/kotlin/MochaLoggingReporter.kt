
import com.zegreatrob.testmints.logs.StructuredLoggingTestMintsReporter
import io.github.oshai.kotlinlogging.KotlinLogging

@JsName("MochaLoggingReporter")
@ExperimentalJsExport
@JsExport
object MochaLoggingReporter {
    private val logger by lazy { KotlinLogging.logger("testmints") }

    fun beforeAll() = StructuredLoggingTestMintsReporter.initialize()

    fun beforeEach(context: MochaContext?) = logger.info {
        mapOf(
            "step" to "setup",
            "state" to "start",
            "name" to context?.currentTest?.fullTitle()?.trim()?.replace(" ", "."),
        )
    }
}

external interface MochaContext {
    val test: MochaTest?
    val currentTest: MochaTest?
}

external interface MochaTest {
    val parent: MochaTest?
    fun fullTitle(): String
    val title: String
}
