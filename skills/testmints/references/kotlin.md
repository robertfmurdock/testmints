# Kotlin Testmints

Use the module and test runner already configured for the project. The examples use `kotlin.test`, with `com.zegreatrob.testmints:standard` for synchronous tests and `com.zegreatrob.testmints:async` for coroutine tests.

## Synchronous test

Use `setup` to expose scenario data to both phases. The exercise return value becomes the verification argument.

```kotlin
import com.zegreatrob.testmints.setup
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelTest {
    @Test
    fun removesOuterWhitespace() = setup(object {
        val label = "release notes"
    }) exercise {
        normalizeLabel("  $label  ")
    } verify { normalized ->
        assertEquals(label, normalized)
    }

    private fun normalizeLabel(raw: String) = raw.trim()
}
```

## Coroutine test

Return the `asyncSetup` chain from the test. The expression-bodied form passes the test result to the runner, including on Kotlin/JS. Call suspending operations directly in their phase. Use `ScopeMint.exerciseScope` when the subject accepts a scope and starts work that verification depends on.

```kotlin
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelStoreTest {
    @Test
    fun savesTheSubmittedLabel() = asyncSetup(object : ScopeMint() {
        val label = "release notes"
        val store = LabelStore(exerciseScope)
    }) exercise {
        store.save(label)
    } verify {
        assertEquals(label, store.savedLabel)
    }

    private class LabelStore(private val scope: CoroutineScope) {
        var savedLabel: String? = null
            private set

        fun save(label: String) {
            scope.launch {
                yield()
                savedLabel = label
            }
        }
    }
}
```

Testmints waits for the direct child jobs it observes in `exerciseScope` at the end of exercise. For work launched outside that scope, await a completion handle or use a bounded wait for an observable result. When successful completion is itself part of the contract, propagate errors from the operation or verify its observable outcome.

## Cleanup and templates

- End with `verify { … }` for an ordinary test. Use `verifyAnd { … } teardown { … }` when that scenario needs an explicit teardown. The teardown has access to setup data. In standard tests it receives the exercise result and is skipped if exercise throws. In async tests it can also run after a failed exercise, with a null result.
- If setup fails before it completes, its phase teardown does not run. Protect partially acquired resources at acquisition, for example by unwinding earlier acquisitions if a later one throws.
- Use `testTemplate` or `asyncTestTemplate` when genuinely shared infrastructure makes several scenarios clearer. The built-in `sharedSetup` and `sharedTeardown` pair brackets each attempt after shared setup succeeds. The `beforeAll` overload supplies a lazy cached value with a different lifetime. Keep scenario inputs and the call under test visible where the template is used.
