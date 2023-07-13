package com.zegreatrob.testmints.action

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.PlatformInfo
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class ActionMintProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val platforms: List<PlatformInfo>
) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = ActionMintVisitor(logger, platforms)
        resolver.getAllFiles().forEach {
            it.accept(visitor, codeGenerator)
        }
        return emptyList()
    }
}


