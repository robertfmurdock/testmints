package com.zegreatrob.testmints.action

interface ExecutableActionPipe {
    fun <D, R> execute(dispatcher: D, action: ExecutableAction<D, R>): R = action.execute(dispatcher)
}

interface ActionWrapper<T> {
    val action: T
}
