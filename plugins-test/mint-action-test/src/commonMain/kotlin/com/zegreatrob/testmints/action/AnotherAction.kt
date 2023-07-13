package com.zegreatrob.testmints.action

data class AnotherAction(val left: Int, val right: Int) : SimpleExecutableAction<AnotherAction.Dispatcher, Int> {
    override val performFunc = link(Dispatcher::doIt)

    fun interface Dispatcher {
        fun doIt(action: AnotherAction): Int
    }
}
