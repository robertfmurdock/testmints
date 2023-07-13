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
            .also { allExecutedActions.add((action as? ActionWrapper<*>)?.action) }
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
            override suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R =
                action.execute(dispatcher)
                    .also { allExecutedActions.add((action as? ActionWrapper<*>)?.action) }
        }
        val cannon = ActionCannon(dispatcher, pipe)
    }) exercise {
        Pair<Any?, Any?>(
            cannon.fire(action = addAction),
            cannon.fire(action = multiplyAction),
        )
    } verify {
        allExecutedActions.assertIsEqualTo(
            listOf(addAction, multiplyAction),
        )
    }
}
