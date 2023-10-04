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
import dev.techullurgy.ksp.processor.extensions.fixSpaces
import dev.techullurgy.ksp.processor.extensions.getAllAbstractFunctions
import dev.techullurgy.ksp.processor.extensions.getOverridableFunSpecBuilder

object DatabaseBuilder {
    fun build(codeGenerator: CodeGenerator, dec: KSClassDeclaration, funSpecs: List<FunSpec>, propertySpecs: List<PropertySpec>) {
        val newFileClassName = ClassName(dec.packageName.asString(), dec.simpleName.asString() + "Impl")
        val typeSpec = TypeSpec.classBuilder(newFileClassName)

        val queryExecutorProperty = PropertySpec.builder("queryExecutor", QueryExecutor::class.asTypeName(), KModifier.PRIVATE)
            .delegate("lazy { %L(databaseConfig) }".fixSpaces(), QueryExecutor::class.qualifiedName)
            .build()

        typeSpec.addProperty(queryExecutorProperty).addProperties(propertySpecs)

        val setAsTransactionFunction = dec.getAllAbstractFunctions().first { it.simpleName.asString() == "setAsTransaction" }

        val setAsTransactionFuncBuilder = setAsTransactionFunction.getOverridableFunSpecBuilder()
        setAsTransactionFuncBuilder.addCode(
            buildCodeBlock {
                addStatement("this.isTransaction = isTransaction")
                addStatement("if(isTransactionMode()) {")
                addStatement("  queryExecutor.beginTransaction()")
                addStatement("} else {")
                addStatement("  queryExecutor.endTransaction()")
                addStatement("}")
            }
        )

        typeSpec.addFunction(setAsTransactionFuncBuilder.build())

        if(dec.modifiers.contains(Modifier.ABSTRACT)) {
            typeSpec.superclass(dec.toClassName())
        } else if(dec.classKind == ClassKind.INTERFACE) {
            typeSpec.addSuperinterface(dec.toClassName())
        }

        val fileSpec = FileSpec.builder(newFileClassName)
            .addType(typeSpec.addFunctions(funSpecs).build())
            .build()

        fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }
}