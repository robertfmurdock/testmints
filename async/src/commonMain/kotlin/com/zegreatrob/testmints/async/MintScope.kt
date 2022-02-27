package com.zegreatrob.testmints.async

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

@DelicateCoroutinesApi
internal fun mintScope() = GlobalScope + SupervisorJob() + CoroutineName("testMintAsync")
