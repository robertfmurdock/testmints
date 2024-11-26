package com.zegreatrob.testmints.action

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class ActionMintTest : ActionPipe {

    @Test
    fun usingTheActionWithTheDispatcherDoesTheWorkOfTheDispatchFunction() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val dispatcher: MultiplyAction.Dispatcher = object : ExampleActionDispatcher {}
    }) exercise {
        execute(dispatcher, action)
    } verify { result ->
        result.assertIsEqualTo(MultiplyAction.Result.Success(6))
    }

    @Test
    fun providesActionWithWrapperFunction() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val dispatcher: MultiplyAction.Dispatcher = object : ExampleActionDispatcher {}
    }) exercise {
        execute(dispatcher, action.wrap())
    } verify { result ->
        result.assertIsEqualTo(MultiplyAction.Result.Success(6))
    }

    @Test
    fun canCallFunctionAsValueWithAutoWrapping() = asyncSetup(object {
        val action = AddAction(2, 3)
        var capturedAction: SuspendAction<*, *>? = null
        val capture = fun (action: SuspendAction<*, *>) {
            capturedAction = action
        }
    }) exercise {
        capture(action)
    } verify {
        capturedAction.assertIsEqualTo(action.wrap())
    }

    @Test
    fun canCallFunctionAsFunWithAutoWrapping() = asyncSetup(object {
        val action = AddAction(2, 3)
        var capturedAction: SuspendAction<*, *>? = null
        fun <D, R> capture(action: SuspendAction<D, R>) {
            capturedAction = action
        }
    }) exercise {
        call(::capture, action)
    } verify {
        capturedAction.assertIsEqualTo(action.wrap())
    }

    @Test
    fun canLetFunctionWithAutoWrapping() = asyncSetup(object {
        val action = AddAction(2, 3)
        var capturedAction: SuspendAction<*, *>? = null
        fun <D, R> capture(action: SuspendAction<D, R>) {
            capturedAction = action
        }
    }) exercise {
        action.let(::capture)
    } verify {
        capturedAction.assertIsEqualTo(action.wrap())
    }

    @Test
    fun executingActionMerelyPassesActionToDispatcherWhereWorkCanBeDone() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val expectedReturn = MultiplyAction.Result.Success(42)
        val spy = SpyData<MultiplyAction, MultiplyAction.Result>().apply { spyWillReturn(expectedReturn) }
        val spyDispatcher = object : ExampleActionDispatcher {
            override suspend fun handle(action: MultiplyAction) = spy.spyFunction(action)
        }
    }) exercise {
        execute(spyDispatcher, action)
    } verify { result ->
        result.assertIsEqualTo(expectedReturn)
        spy.spyReceivedValues.assertIsEqualTo(listOf(action))
    }

    @Test
    fun singleDispatcherObjectCanExecuteManyActions() = asyncSetup(object {
        val dispatcher = object : AddActionDispatcher, ExampleActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)
    }) exercise {
        Pair(
            execute(dispatcher, addAction),
            execute(dispatcher, multiplyAction),
        )
    } verify { result ->
        with(result) {
            first.assertIsEqualTo(29)
            second.assertIsEqualTo(MultiplyAction.Result.Success(533))
        }
    }

    @Test
    fun usingActionPipeAllowsInterceptionOfAnyAction() = asyncSetup(object : ActionPipe {
        val dispatcher = object : AddActionDispatcher, ExampleActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)

        val allExecutedActions = mutableListOf<Any?>()

        override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R = action.execute(dispatcher)
            .also { allExecutedActions.add((action as? ActionWrapper<*, *>)?.action) }
    }) exercise {
        Pair(
            execute(dispatcher, addAction),
            execute(dispatcher, multiplyAction),
        )
    } verify {
        allExecutedActions.assertIsEqualTo(
            listOf(addAction, multiplyAction),
        )
    }

    @Test
    fun worksWithCannon() = asyncSetup(object {
        val dispatcher = object : AddActionDispatcher, ExampleActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)

        val allExecutedActions = mutableListOf<Any?>()

        val pipe = object : ActionPipe {
            override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R = action.execute(dispatcher)
                .also { allExecutedActions.add((action as? ActionWrapper<*, *>)?.action) }
        }
        val cannon = ActionCannon(dispatcher, pipe)
    }) exercise {
        Pair<Any?, Any?>(
            cannon.fire(action = addAction),
            perform(cannon, action = multiplyAction),
        )
    } verify {
        allExecutedActions.assertIsEqualTo(
            listOf(addAction, multiplyAction),
        )
    }

    @Test
    fun worksWithGenericReturnTypes() = asyncSetup(object {
        val dispatcher = object : ResultAction.Dispatcher {
            override fun handle(action: ResultAction) = runCatching { action.left + action.right }
        }
        val resultAction = ResultAction(7, 22)

        val allExecutedActions = mutableListOf<Any?>()

        val pipe = object : ActionPipe {
            override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R = action.execute(dispatcher)
                .also { allExecutedActions.add((action as? ActionWrapper<*, *>)?.action) }
        }
        val cannon = ActionCannon(dispatcher, pipe)
    }) exercise {
        cannon.fire(action = resultAction)
    } verify { result: Result<Int> ->
        result.assertIsEqualTo(Result.success(29))
    }

    @Test
    fun worksWithGenericDispatcherTypes() = asyncSetup(object {
        val dispatcher = object : WildAction.Dispatcher<Int, String, Boolean> {
            override fun handle(action: WildAction) = runCatching { action.left + action.right }
        }
        val wildAction = WildAction(7, 22)

        val allExecutedActions = mutableListOf<Any?>()

        val pipe = object : ActionPipe {
            override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R = action.execute(dispatcher)
                .also { allExecutedActions.add((action as? ActionWrapper<*, *>)?.action) }
        }
        val cannon = ActionCannon(dispatcher, pipe)
    }) exercise {
        cannon.fire(action = wildAction)
    } verify { result: Result<Int> ->
        result.assertIsEqualTo(Result.success(29))
    }
}
