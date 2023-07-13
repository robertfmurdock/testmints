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
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
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
        val actionWrapperClassName = ClassName(
            actionDeclaration.packageName.asString(),
            "${actionDeclaration.simpleName.asString()}Wrapper"
        )
        FileSpec.builder(
            packageName = dispatcherDeclaration.packageName.asString(),
            fileName = executeFileName(actionDeclaration, dispatcherDeclaration)
        )
            .addType(
                TypeSpec.classBuilder(actionWrapperClassName)
                    .addModifiers(KModifier.DATA)
                    .addSuperinterface(
                        ClassName("com.zegreatrob.testmints.action.async", "SuspendAction").parameterizedBy(
                            dispatcherDeclaration.toClassName(),
                            resultType.toTypeName()
                        )
                    )
                    .addSuperinterface(
                        ClassName("com.zegreatrob.testmints.action", "ActionWrapper").parameterizedBy(
                            actionDeclaration.toClassName()
                        )
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("action", actionDeclaration.toClassName())
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("action", actionDeclaration.toClassName())
                            .initializer("action")
                            .addModifiers(KModifier.OVERRIDE)
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("execute")
                            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                            .addParameter("dispatcher", dispatcherDeclaration.toClassName())
                            .addCode("return dispatcher.${dispatcherFunction.simpleName.asString()}(action)")
                            .returns(returnType = resultType.toTypeName())
                            .build()
                    )
                    .build()
            )

            .addFunction(
                FunSpec.builder("execute")
                    .addModifiers(KModifier.SUSPEND)
                    .receiver(ClassName("com.zegreatrob.testmints.action", "ExecutableActionPipe"))
                    .addParameter("dispatcher", dispatcherDeclaration.toClassName())
                    .addParameter("action", actionDeclaration.toClassName())
                    .returns(resultType.toTypeName(TypeParameterResolver.EMPTY))
                    .addCode(
                        "return execute(dispatcher, %L.invoke(action))",
                        actionWrapperClassName.constructorReference()
                    )
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
