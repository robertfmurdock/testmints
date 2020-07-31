package com.zegreatrob.react.dataloader

import com.zegreatrob.minreact.reactFunction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import react.FunctionalComponent
import react.RProps
import react.RSetState
import react.useState

typealias DataLoadFunc<D> = suspend (DataLoaderTools) -> D

data class DataLoadWrapperProps<D>(
    val getDataAsync: DataLoadFunc<D>,
    val errorData: (Throwable) -> D,
    val scope: CoroutineScope? = null
) : RProps

private val cachedComponent = reactFunction<DataLoadWrapperProps<out Any>> { props ->
    val (getDataAsync, errorData, injectedScope) = props
    val (state, setState) = useState<DataLoadState<out Any>> { EmptyState() }
    val scope = injectedScope ?: useScope("Data load")

    if (state is EmptyState) {
        startPendingJob(scope, setState, getDataAsync, errorData)
    }

    props.children(state)
}

fun <D> dataLoader() = cachedComponent.unsafeCast<FunctionalComponent<DataLoadWrapperProps<D>>>()

private fun <D> startPendingJob(
    scope: CoroutineScope,
    setState: RSetState<DataLoadState<D>>,
    getDataAsync: DataLoadFunc<D>,
    errorData: (Throwable) -> D
) {
    val setEmpty = setState.empty()
    val setPending = setState.pending()
    val setResolved = setState.resolved()
    val tools = DataLoaderTools(scope, setEmpty)
    setPending(
        scope.launch { getDataAsync(tools).let(setResolved) }
            .also { job -> job.errorOnJobFailure(setResolved, errorData) }
    )
}

private fun <D> Job.errorOnJobFailure(setResolved: (D) -> Unit, errorResult: (Throwable) -> D) =
    invokeOnCompletion { cause -> if (cause != null) setResolved(errorResult(cause)) }

private fun <D> RSetState<DataLoadState<D>>.empty(): () -> Unit = { this(
    EmptyState()
) }

private fun <D> RSetState<DataLoadState<D>>.pending(): (Job) -> Unit = { this(
    PendingState()
) }

private fun <D> RSetState<DataLoadState<D>>.resolved(): (D) -> Unit = { this(
    ResolvedState(it)
) }