package com.zegreatrob.testmints.action

import com.zegreatrob.testmints.action.async.SuspendAction

interface ActionCannon<out D> {
    suspend fun <R> fire(action: SuspendAction<D, R>): R

    companion object {
        operator fun <D> invoke(dispatcher: D, pipe: ActionPipe = ActionPipe): ActionCannon<D> =
             DispatcherPipeCannon(dispatcher, pipe)
    }
}

data class DispatcherPipeCannon<out D>(val dispatcher: D, val pipe: ActionPipe = ActionPipe) : ActionCannon<D> {
    override suspend fun <R> fire(action: SuspendAction<D, R>): R = pipe.execute(dispatcher, action)
}
