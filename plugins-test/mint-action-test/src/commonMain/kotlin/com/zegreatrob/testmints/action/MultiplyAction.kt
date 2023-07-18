package com.zegreatrob.testmints.action

import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class MultiplyAction(val left: Int, val right: Int) {

    interface Dispatcher {
        suspend fun handle(action: MultiplyAction): Result
    }

    sealed interface Result {
        data class Success(val value: Int) : Result
        data class Disconnected(val message: String) : Result
    }
}

interface ExampleActionDispatcher : MultiplyAction.Dispatcher {
    override suspend fun handle(action: MultiplyAction): MultiplyAction.Result =
        MultiplyAction.Result.Success(action.left * action.right)
}

@ActionMint
data class AddAction(val left: Int, val right: Int) {

    interface Dispatcher {
        fun handle(action: AddAction): Int
    }
}

interface AddActionDispatcher : AddAction.Dispatcher {
    override fun handle(action: AddAction): Int = with(action) { left + right }
}

@ActionMint
data class ResultAction(val left: Int, val right: Int) {

    interface Dispatcher {
        fun handle(action: ResultAction): Result<Int>
    }
}

@ActionMint
data class WildAction(val left: Int, val right: Int) {

    interface Dispatcher<T, T2, T3> {
        fun handle(action: WildAction): Result<Int>
    }
}
