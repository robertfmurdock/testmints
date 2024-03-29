package com.zegreatrob.testmints.action

import com.zegreatrob.testmints.action.async.SuspendAction

interface ActionPipe {
    suspend fun <D, R> execute(dispatcher: D, action: SuspendAction<D, R>): R = action.execute(dispatcher)
    companion object : ActionPipe
}
