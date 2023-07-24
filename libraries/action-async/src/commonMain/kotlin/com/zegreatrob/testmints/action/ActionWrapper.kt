package com.zegreatrob.testmints.action

import kotlin.reflect.KClass

interface ActionWrapper<D : Any, T : Any> {
    val action: T
    val dispatcherType: KClass<D>
}
