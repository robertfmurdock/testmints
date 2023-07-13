package com.zegreatrob.testmints.action

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider


class ActionMintProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = ActionMintProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger
    )
}