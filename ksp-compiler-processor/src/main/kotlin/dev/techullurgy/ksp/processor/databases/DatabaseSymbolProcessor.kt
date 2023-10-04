package dev.techullurgy.ksp.processor.databases

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.techullurgy.ksp.annotations.Dao
import dev.techullurgy.ksp.annotations.Database
import dev.techullurgy.ksp.processor.builders.DatabaseBuilder
import dev.techullurgy.ksp.processor.extensions.fixSpaces
import dev.techullurgy.ksp.processor.extensions.getAllAbstractFunctionsOfReturnTypeAnnotatedWith
import dev.techullurgy.ksp.processor.extensions.getOverridableFunSpecBuilder
import java.util.*

class DatabaseSymbolProcessor(
    private val codeGenerator: CodeGenerator
): SymbolProcessor {

    private var funSpecs = mutableListOf<FunSpec>()
    private var propertySpecs = mutableListOf<PropertySpec>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Database::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>().toList()
        require(symbols.size <= 1) { "@Database should be available for one class" }
        if(symbols.isEmpty()) return emptyList()

        symbols[0].accept(DatabaseVisitor(), Unit)
        writeFile(symbols[0])
        clearAll()
        return emptyList()
    }

    private fun writeFile(dec: KSClassDeclaration) {
        DatabaseBuilder.build(codeGenerator, dec, funSpecs, propertySpecs)
    }

    private fun clearAll() {
        funSpecs = mutableListOf()
        propertySpecs = mutableListOf()
    }

    private inner class DatabaseVisitor: KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            require(classDeclaration.classKind == ClassKind.INTERFACE || classDeclaration.modifiers.contains(Modifier.ABSTRACT)) {
                "@Database should be available for abstract class or interface"
            }

            val daoFunctions = classDeclaration.getAllAbstractFunctionsOfReturnTypeAnnotatedWith(Dao::class)

            daoFunctions.forEach { it.accept(this, Unit) }
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val returnTypeString = function.returnType!!.resolve().declaration.simpleName.asString()
            val returnTypeName = function.returnType!!.toTypeName()
            val propertySpec = PropertySpec.builder(
                "_${returnTypeString.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                returnTypeName,
                KModifier.PRIVATE
            )
                .delegate("lazy { %L(queryExecutor, this) }".fixSpaces(), returnTypeName.toString()+"Implementation")
                .build()

            propertySpecs.add(propertySpec)

            val funSpecBuilder = function.getOverridableFunSpecBuilder()
            val codeBlock = buildCodeBlock {
                add("return %L", propertySpec.name)
            }
            funSpecs.add(funSpecBuilder.addCode(codeBlock).build())
        }
    }
}