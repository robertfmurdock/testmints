import kotlin.js.json

@ExperimentalJsExport
@JsExport
fun mochaHooks() = json(
    "beforeAll" to { done: () -> Unit ->
        MochaLoggingReporter.beforeAll()
        done()
    },
    "beforeEach" to { done: () -> Unit ->
        MochaLoggingReporter.beforeEach(js("this"))
        done()
    },
)
