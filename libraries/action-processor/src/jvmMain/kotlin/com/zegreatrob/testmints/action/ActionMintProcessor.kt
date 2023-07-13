package com.zegreatrob.testmints.action

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSTopDownVisitor

class ActionMintProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = ActionMintVisitor(logger)
        resolver.getAllFiles().forEach {
            it.accept(visitor, codeGenerator)
        }
        return emptyList()
    }
}

class ActionMintVisitor(private val logger: KSPLogger) : KSTopDownVisitor<CodeGenerator, Unit>() {
    override fun defaultHandler(node: KSNode, data: CodeGenerator) {
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: CodeGenerator) {
        super.visitClassDeclaration(classDeclaration, data)
        logger.warn("hi ${classDeclaration.simpleName.asString()}")
    }
}

class ActionMintProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = ActionMintProcessor(
        codeGenerator = environment.codeGenerator,
        logger = environment.logger
    )
}