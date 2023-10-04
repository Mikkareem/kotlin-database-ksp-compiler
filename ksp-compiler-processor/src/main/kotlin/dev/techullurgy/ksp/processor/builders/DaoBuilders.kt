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
import dev.techullurgy.ksp.processor.extensions.constructorProperties
import java.sql.ResultSet

object DaoBuilders {
    fun build(codeGenerator: CodeGenerator, dec: KSClassDeclaration, funSpecs: List<FunSpec>) {
        val newFileClassName = ClassName(dec.packageName.asString(), dec.simpleName.asString() + "Implementation")
        val typeSpec = TypeSpec.classBuilder(newFileClassName)

        typeSpec.constructorProperties(
            listOf(
                PropertySpec.builder("queryExecutor", QueryExecutor::class.asTypeName(), KModifier.PRIVATE).build(),
                PropertySpec.builder("roomDatabase", RoomDatabase::class.asTypeName(), KModifier.PRIVATE).build()
            )
        )

        if(dec.modifiers.contains(Modifier.ABSTRACT)) {
            typeSpec.superclass(dec.toClassName())
        } else if(dec.classKind == ClassKind.INTERFACE) {
            typeSpec.addSuperinterface(dec.toClassName())
        }

        val executeDmlFunc = FunSpec.builder("executeDml")
            .addModifiers(KModifier.PRIVATE)
            .addParameter(
                ParameterSpec("dml", String::class.asTypeName())
            )
            .addCode(buildExecuteFunctionForDml())

        typeSpec.addFunction(executeDmlFunc.build())

        val executeSqlFunc = FunSpec.builder("executeSql")
            .addModifiers(KModifier.PRIVATE)
            .addParameter(
                ParameterSpec("sql", String::class.asTypeName())
            )
            .addCode(buildExecuteFunctionForSql())

        typeSpec.addFunction(executeSqlFunc.build())

        val fileSpec = FileSpec.builder(newFileClassName)
            .addType(
                typeSpec
                    .addFunctions(funSpecs)
                    .build()
            )
            .build()

        fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }

    private fun buildExecuteFunctionForDml(): CodeBlock {
        return buildCodeBlock {
            addStatement("if(roomDatabase.isTransactionMode()) {")
            addStatement("  queryExecutor.executeTransactionDML(dml)")
            addStatement("} else {")
            addStatement("  queryExecutor.executeNonTransactionDML(dml)")
            addStatement("}")
        }
    }

    private fun buildExecuteFunctionForSql(): CodeBlock {
        return buildCodeBlock {
            addStatement("val callback: (%L) -> Unit = {}", ResultSet::class.qualifiedName)
            addStatement("if(roomDatabase.isTransactionMode()) {")
            addStatement("  queryExecutor.executeTransactionSQL(sql, callback)")
            addStatement("} else {")
            addStatement("  queryExecutor.executeNonTransactionSQL(sql, callback)")
            addStatement("}")
        }
    }
}