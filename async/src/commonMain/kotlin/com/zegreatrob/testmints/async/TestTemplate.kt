package com.zegreatrob.testmints.async

import com.zegreatrob.testmints.report.ReporterProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async

class TestTemplate<out SC : Any>(
    val reporterProvider: ReporterProvider,
    private val templateScope: CoroutineScope = mintScope(),
    val wrapper: suspend (TestFunc<SC>) -> Unit
) {

    fun <SC2 : Any> extend(wrapper: suspend (SC, TestFunc<SC2>) -> Unit) = TestTemplate<SC2>(reporterProvider) { test ->
        this.wrapper { sc1 -> wrapper(sc1, test) }
    }

    fun <SC2 : Any> extend(sharedSetup: suspend (SC) -> SC2, sharedTeardown: suspend (SC2) -> Unit = {}) =
        extend<SC2> { sc1, test ->
            val sc2 = sharedSetup(sc1)
            test(sc2)
            sharedTeardown(sc2)
        }

    fun extend(sharedSetup: suspend () -> Unit = {}, sharedTeardown: suspend () -> Unit = {}) = TestTemplate<SC>(
        reporterProvider,
    ) { test ->
        wrapper {
            sharedSetup()
            test(it)
            sharedTeardown()
        }
    }

    fun <BAC : Any> extend(beforeAll: suspend () -> BAC): TestTemplate<BAC> = extend(
        beforeAll = beforeAll,
        mergeContext = { _, bac -> bac }
    )

    fun <BAC : Any, SC2 : Any> extend(
        beforeAll: suspend () -> BAC,
        mergeContext: suspend (SC, BAC) -> SC2
    ): TestTemplate<SC2> {
        val deferred = templateScope.async(start = CoroutineStart.LAZY) { beforeAll() }
        return extend(sharedSetup = { sharedContext ->
            mergeContext(sharedContext, deferred.await())
        })
    }

    operator fun <C : Any> invoke(context: C, timeoutMs: Long = 60_000L, additionalActions: suspend C.() -> Unit = {}) =
        Setup(
            { context },
            context.chooseTestScope(),
            additionalActions,
            reporterProvider.reporter,
            timeoutMs,
            wrapper
        )

    operator fun invoke(timeoutMs: Long = 60_000L, additionalActions: suspend SC.() -> Unit = {}) = Setup(
        { it },
        mintScope(),
        additionalActions,
        reporterProvider.reporter,
        timeoutMs,
        wrapper
    )

    fun <C : Any> with(
        contextProvider: suspend (SC) -> C,
        timeoutMs: Long = 60_000L,
        additionalActions: suspend C.() -> Unit = {}
    ) = Setup(contextProvider, mintScope(), additionalActions, reporterProvider.reporter, timeoutMs, wrapper)
}

fun <C : Any> TestTemplate<Unit>.with(
    contextProvider: suspend () -> C,
    timeoutMs: Long = 60_000L,
    additionalAction: suspend C.() -> Unit = {}
): Setup<C, Unit> {
    val unitSharedContextAdapter: suspend (Unit) -> C = { contextProvider() }
    return with(unitSharedContextAdapter, timeoutMs, additionalAction)
}
