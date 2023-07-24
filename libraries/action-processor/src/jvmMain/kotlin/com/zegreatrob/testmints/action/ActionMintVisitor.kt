package com.zegreatrob.testmints.action

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.JvmPlatformInfo
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.PlatformInfo
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

val mintActionClassName = ClassName("com.zegreatrob.testmints.action.annotation", "MintAction")
val actionMintClassName = ClassName("com.zegreatrob.testmints.action.annotation", "ActionMint")
val actionCannonClassName = ClassName("com.zegreatrob.testmints.action", "ActionCannon")

class ActionMintVisitor(private val logger: KSPLogger, private val platforms: List<PlatformInfo>) :
    KSTopDownVisitor<CodeGenerator, Unit>() {
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
            writeExecuteFunction(
                actionDeclaration = parentDeclaration,
                dispatcherDeclaration = classDeclaration,
                dispatcherFunction = dispatcherFunction,
                codeGenerator = data,
                resultType = dispatchReturnType.resolve().toTypeName()
            )
        }
    }

    private fun writeExecuteFunction(
        actionDeclaration: KSClassDeclaration,
        dispatcherDeclaration: KSClassDeclaration,
        dispatcherFunction: KSFunctionDeclaration,
        codeGenerator: CodeGenerator,
        resultType: TypeName
    ) {
        val actionWrapperClassName = ClassName(
            actionDeclaration.packageName.asString(),
            "${actionDeclaration.simpleName.asString()}Wrapper"
        )

        val isJvm = platforms.any { it is JvmPlatformInfo }
        FileSpec.builder(
            packageName = dispatcherDeclaration.packageName.asString(),
            fileName = executeFileName(actionDeclaration, dispatcherDeclaration)
        )
            .addType(
                TypeSpec.classBuilder(actionWrapperClassName)
                    .addAnnotations(
                        if (isJvm) listOf(
                            AnnotationSpec.builder(ClassName("kotlin.jvm", "JvmInline")).build()
                        ) else emptyList()
                    )
                    .addModifiers(KModifier.VALUE)
                    .addSuperinterface(
                        ClassName("com.zegreatrob.testmints.action.async", "SuspendAction").parameterizedBy(
                            dispatcherDeclaration.classNameWithStar(),
                            resultType
                        )
                    )
                    .addSuperinterface(
                        ClassName("com.zegreatrob.testmints.action", "ActionWrapper").parameterizedBy(
                            dispatcherDeclaration.classNameWithStar(),
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
                    .addProperty(
                        PropertySpec.builder(
                            name = "dispatcherType",
                            type = ClassName("kotlin.reflect","KClass").parameterizedBy(dispatcherDeclaration.classNameWithStar())
                        )
                            .getter(FunSpec.getterBuilder()
                                .addCode("return ${dispatcherDeclaration.qualifiedName?.asString()}::class")
                                .build())
                            .addModifiers(KModifier.OVERRIDE)
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("execute")
                            .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                            .addParameter("dispatcher", dispatcherDeclaration.classNameWithStar())
                            .addCode("return dispatcher.${dispatcherFunction.simpleName.asString()}(action)")
                            .returns(returnType = resultType)
                            .build()
                    )
                    .build()
            )

            .addFunction(
                FunSpec.builder("execute")
                    .addModifiers(KModifier.SUSPEND)
                    .receiver(ClassName("com.zegreatrob.testmints.action", "ActionPipe"))
                    .addParameter("dispatcher", dispatcherDeclaration.classNameWithStar())
                    .addParameter("action", actionDeclaration.toClassName())
                    .returns(resultType)
                    .addCode(
                        "return execute(dispatcher, %L.invoke(action))",
                        actionWrapperClassName.constructorReference()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("perform")
                    .addModifiers(KModifier.SUSPEND)
                    .addParameter("cannon", actionCannonClassName.parameterizedBy(dispatcherDeclaration.classNameWithStar()))
                    .addParameter("action", actionDeclaration.toClassName())
                    .returns(resultType)
                    .addCode(
                        "return cannon.fire(%L.invoke(action))",
                        actionWrapperClassName.constructorReference()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("fire")
                    .addModifiers(KModifier.SUSPEND)
                    .receiver(actionCannonClassName.parameterizedBy(dispatcherDeclaration.classNameWithStar()))
                    .addParameter("action", actionDeclaration.toClassName())
                    .returns(resultType)
                    .addCode(
                        "return fire(%L.invoke(action))",
                        actionWrapperClassName.constructorReference()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("wrap")
                    .receiver(actionDeclaration.classNameWithStar())
                    .returns(actionWrapperClassName)
                    .addCode(
                        "return %L.invoke(this)",
                        actionWrapperClassName.constructorReference()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OPERATOR)
                    .receiver(
                        LambdaTypeName.get(
                        receiver = actionWrapperClassName,
                        parameters = emptyList(),
                        returnType = UNIT
                    ))
                    .addParameter("action", actionDeclaration.toClassName())
                    .returns(Unit::class)
                    .addCode(
                        "return this(%L.invoke(action))",
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

    private fun KSClassDeclaration.classNameWithStar() = if (typeParameters.isEmpty()) {
        toClassName()
    } else {
        toClassName().parameterizedBy(
            typeArguments = (1..typeParameters.size).map { STAR }
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
            || toAnnotationSpec().typeName == actionMintClassName
}
