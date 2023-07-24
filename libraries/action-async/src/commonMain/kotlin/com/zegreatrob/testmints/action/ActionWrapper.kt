package com.zegreatrob.testmints.action

import kotlin.reflect.KClass

interface ActionWrapper<D : Any, T> {
    val action: T
    val dispatcherType: KClass<D>
}
