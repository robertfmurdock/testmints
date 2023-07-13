package com.zegreatrob.testmints.action

import com.zegreatrob.testmints.action.async.SuspendAction

data class ActionCannon<out D>(val dispatcher: D, val pipe: ActionPipe = ActionPipe) {
    suspend fun <R> fire(action: SuspendAction<D, R>): R = pipe.execute(dispatcher, action)
}
