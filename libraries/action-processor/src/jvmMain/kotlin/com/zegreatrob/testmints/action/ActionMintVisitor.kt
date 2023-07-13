package com.zegreatrob.testmints.action

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

val mintActionClassName = ClassName("com.zegreatrob.testmints.action.annotation", "MintAction")

class ActionMintVisitor(private val logger: KSPLogger) : KSTopDownVisitor<CodeGenerator, Unit>() {
    override fun defaultHandler(node: KSNode, data: CodeGenerator) {
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: CodeGenerator) {
        super.visitClassDeclaration(classDeclaration, data)

        val parentDeclaration = classDeclaration.parentDeclaration as? KSClassDeclaration
            ?: return

        logger.warn("CLASS ${classDeclaration.simpleName.asString()} PARENT $parentDeclaration")

        if (parentDeclaration.hasActionMint() && classDeclaration.isActionDispatcher()) {
            val dispatcherFunction = classDeclaration.getDeclaredFunctions().firstOrNull()
                ?: return

            val dispatchReturnType = dispatcherFunction.returnType
                ?: return
            writeExecuteFunction(parentDeclaration, classDeclaration, dispatcherFunction, dispatchReturnType, data)

        }
    }

    private fun writeExecuteFunction(
        actionDeclaration: KSClassDeclaration,
        dispatcherDeclaration: KSClassDeclaration,
        dispatcherFunction: KSFunctionDeclaration,
        resultType: KSTypeReference,
        codeGenerator: CodeGenerator
    ) {
        FileSpec.builder(
            packageName = dispatcherDeclaration.packageName.asString(),
            fileName = executeFileName(actionDeclaration, dispatcherDeclaration)
        )
            .addFunction(
                FunSpec.builder("execute")
                    .receiver(dispatcherDeclaration.toClassName())
                    .addParameter("action", actionDeclaration.toClassName())
                    .returns(resultType.toTypeName(TypeParameterResolver.EMPTY))
                    .addCode("return ${dispatcherFunction.simpleName.asString()}(action)")
                    .build()
            )
            .build()
            .writeTo(
                codeGenerator, Dependencies(
                    aggregating = false,
                    sources = setOfNotNull(dispatcherDeclaration.containingFile, actionDeclaration.containingFile)
                        .toTypedArray()
                )
            )
    }

    private fun executeFileName(
        parentDeclaration: KSClassDeclaration,
        classDeclaration: KSClassDeclaration
    ) = "${parentDeclaration.simpleName.asString()}${classDeclaration.simpleName.asString()}ExecuteKt"

}

private fun KSClassDeclaration.hasActionMint() = annotations.any(KSAnnotation::hasActionMint)

private fun KSClassDeclaration.isActionDispatcher() = simpleName.asString() == "Dispatcher"

private fun KSAnnotation.hasActionMint(): Boolean {
    return toAnnotationSpec().typeName == mintActionClassName
}
