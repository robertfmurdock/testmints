package com.zegreatrob.testmints.action

interface SimpleExecutableAction<D, R> : ExecutableAction<D, R> {
    override fun execute(dispatcher: D) = performFunc(dispatcher)
    val performFunc: (D) -> R
    fun <A> A.link(performFunc: (D, A) -> R): (D) -> R = { performFunc(it, this) }
}
