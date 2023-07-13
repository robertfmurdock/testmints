package com.zegreatrob.testmints.action

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class ActionMintTest {

    @Test
    fun usingTheActionWithTheDispatcherDoesTheWorkOfTheDispatchFunction() = setup(object {
        val action = MultiplyAction(2, 3)
        val dispatcher: MultiplyAction.Dispatcher = object : ExampleActionDispatcher {}
    }) exercise {
        dispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(MultiplyAction.Result.Success(6))
    }

    @Test
    fun executingActionMerelyPassesActionToDispatcherWhereWorkCanBeDone() = setup(object {
        val action = MultiplyAction(2, 3)
        val expectedReturn = MultiplyAction.Result.Success(42)
        val spy = SpyData<MultiplyAction, MultiplyAction.Result>().apply { spyWillReturn(expectedReturn) }
        val spyDispatcher = object : ExampleActionDispatcher {
            override fun handle(action: MultiplyAction) = spy.spyFunction(action)
        }
    }) exercise {
        spyDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReturn)
        spy.spyReceivedValues.assertIsEqualTo(listOf(action))
    }

    @Test
    fun singleDispatcherObjectCanExecuteManyActions() = setup(object {
        val dispatcher = object : AddActionDispatcher, ExampleActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)
    }) exercise {
        Pair(
            dispatcher.execute(addAction),
            dispatcher.execute(multiplyAction),
        )
    } verify { result ->
        with(result) {
            first.assertIsEqualTo(29)
            second.assertIsEqualTo(MultiplyAction.Result.Success(533))
        }
    }

    @Test
    fun usingExecutableActionSyntaxAllowsInterceptionOfAnyAction() = setup(object : ExecutableActionExecuteSyntax {
        val dispatcher = object : AddActionDispatcher, ExampleActionDispatcher {}
        val addAction = AddAction(7, 22)
        val multiplyAction = MultiplyAction(13, 41)

        val allExecutedActions = mutableListOf<Any>()
        override fun <D, R> D.execute(action: ExecutableAction<D, R>) = action.execute(this)
            .also { allExecutedActions.add(action) }
    }) exercise {
        Pair(
            dispatcher.execute(addAction),
            dispatcher.execute(multiplyAction),
        )
    } verify {
        allExecutedActions.assertIsEqualTo(
            listOf(addAction, multiplyAction),
        )
    }
}
