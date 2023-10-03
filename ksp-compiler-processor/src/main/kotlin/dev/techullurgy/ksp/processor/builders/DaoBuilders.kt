package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.techullurgy.ksp.QueryExecutor
import dev.techullurgy.ksp.RoomDatabase
import dev.techullurgy.ksp.processor.extensions.constructorProperty

object DaoBuilders {
    fun build(codeGenerator: CodeGenerator, dec: KSClassDeclaration, funSpecs: List<FunSpec>) {
        val newFileClassName = ClassName(dec.packageName.asString(), dec.simpleName.asString() + "Implementation")
        val typeSpec = TypeSpec.classBuilder(newFileClassName)

        typeSpec.addProperty(
            PropertySpec.builder("isTransaction", Boolean::class.asTypeName(), KModifier.PRIVATE)
                .mutable()
                .initializer("false")
                .build()
        )

        typeSpec.constructorProperty("queryExecutor", QueryExecutor::class.asTypeName(), KModifier.PRIVATE)

        if(dec.modifiers.contains(Modifier.ABSTRACT)) {
            typeSpec.superclass(dec.toClassName())
        } else if(dec.classKind == ClassKind.INTERFACE) {
            typeSpec.addSuperinterface(dec.toClassName())
        }

        val setAsTransactionFunc = FunSpec.builder("setAsTransaction")
            .addModifiers(KModifier.PRIVATE)
            .addParameter(
                ParameterSpec("isTransaction", Boolean::class.asTypeName())
            )
            .addCode(buildSetTransactionFunctionContent())

        val executeDmlFunc = FunSpec.builder("executeDml")
            .addModifiers(KModifier.PRIVATE)
            .addParameter(
                ParameterSpec("dml", String::class.asTypeName())
            )
            .addCode(buildExecuteFunctionForDml())

        typeSpec.addFunction(setAsTransactionFunc.build())
        typeSpec.addFunction(executeDmlFunc.build())

        val fileSpec = FileSpec.builder(newFileClassName)
            .addType(
                typeSpec
                    .addFunctions(funSpecs)
                    .build()
            )
            .build()

        fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }

    private fun buildSetTransactionFunctionContent(): CodeBlock {
        return buildCodeBlock {
            add("""
                |this.isTransaction = isTransaction
                |if(this.isTransaction) {
                |   queryExecutor.beginTransaction()
                |} else {
                |   queryExecutor.endTransaction()
                |}
            """.trimMargin())
        }
    }

    private fun buildExecuteFunctionForDml(): CodeBlock {
        return buildCodeBlock {
            addStatement("if(isTransaction) {")
            addStatement("  queryExecutor.executeTransactionDML(dml)")
            addStatement("} else {")
            addStatement("  queryExecutor.executeNonTransactionDML(dml)")
            addStatement("}")
        }
    }
}