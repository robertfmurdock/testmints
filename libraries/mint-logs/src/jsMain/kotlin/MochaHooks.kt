import kotlin.js.json

@ExperimentalJsExport
@JsExport
fun mochaHooks() = json(
    "beforeEach" to { done: () -> Unit ->
        MochaLoggingReporter.beforeEach(js("this"))
        done()
    },
)
