package dev.techullurgy.ksp.processor.daos

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.techullurgy.ksp.annotations.Dao
import dev.techullurgy.ksp.annotations.Entity
import dev.techullurgy.ksp.annotations.Insert
import dev.techullurgy.ksp.annotations.Update
import dev.techullurgy.ksp.processor.builders.DaoBuilders
import dev.techullurgy.ksp.processor.builders.QueryBuilder
import dev.techullurgy.ksp.processor.extensions.isAnnotatedWith

class DaosSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {

    private var funSpecs: MutableList<FunSpec> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val daos = resolver.getSymbolsWithAnnotation(Dao::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()
        daos.forEach {
            it.accept(DaoVisitor(), Unit)
            writeFile(it)
            clearAll()
        }
        return emptyList()
    }

    private fun writeFile(dec: KSClassDeclaration) {
        DaoBuilders.build(codeGenerator, dec, funSpecs)
    }

    private fun clearAll() {
        funSpecs = mutableListOf()
    }

    private inner class DaoVisitor: KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if(classDeclaration.classKind != ClassKind.INTERFACE && !classDeclaration.modifiers.contains(Modifier.ABSTRACT)) {
                logger.error("@Dao will apply only to either interfaces or abstract classes")
                return
            }

            classDeclaration.getAllFunctions()
                .filter { it.isAbstract }
                .forEach {
                    it.accept(this, Unit)
                }
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            if(function.isAnnotatedWith(Insert::class)) {
                handleInsertAnnotatedFunction(function)
            } else if(function.isAnnotatedWith(Update::class)) {
                handleUpdateAnnotatedFunction(function)
            }
        }

        private fun handleUpdateAnnotatedFunction(function: KSFunctionDeclaration) {
            val parameters = function.parameters
            require(parameters.size == 1) { "@Update annotated functions should have only one parameter of Entity" }
            val entityParameter = parameters.firstOrNull {
                (it.type.resolve().declaration as KSClassDeclaration).isAnnotatedWith(Entity::class)
            }
            require(entityParameter != null) { "@Update annotated functions should have Entity parameter" }

            val entityClassDeclaration = entityParameter.type.resolve().declaration as KSClassDeclaration
            val entityParameterName = entityParameter.name!!.asString()

            val codeBlock = QueryBuilder.buildUpdateQuery(entityParameterName, entityClassDeclaration)
            buildEntityFunctions(entityParameterName, "updateFor", entityClassDeclaration, function, codeBlock)
        }

        private fun handleInsertAnnotatedFunction(function: KSFunctionDeclaration) {
            val parameters = function.parameters
            require(parameters.size == 1) { "@Insert annotated functions should have only one parameter of Entity" }
            val entityParameter = parameters.firstOrNull {
                (it.type.resolve().declaration as KSClassDeclaration).isAnnotatedWith(Entity::class)
            }
            require(entityParameter != null) { "@Insert annotated functions should have Entity parameter" }

            val entityClassDeclaration = entityParameter.type.resolve().declaration as KSClassDeclaration
            val entityParameterName = entityParameter.name!!.asString()

            val codeBlock = QueryBuilder.buildInsertQuery(entityParameterName, entityClassDeclaration)

            buildEntityFunctions(entityParameterName, "insertFor", entityClassDeclaration, function, codeBlock)
        }

        private fun buildEntityFunctions(entityParameterName: String, funNamePrefix: String, entityClassDeclaration: KSClassDeclaration, function: KSFunctionDeclaration, codeBlock: CodeBlock) {
            val queryFunc = FunSpec.builder("${funNamePrefix}${entityClassDeclaration.simpleName.asString()}")
                .addModifiers(KModifier.PRIVATE)
                .addParameter(ParameterSpec(entityParameterName, entityClassDeclaration.toClassName()))
                .returns(String::class)
                .addCode(codeBlock)
                .build()

            val parameterSpecs = function.parameters.map {
                ParameterSpec(it.name!!.asString(), it.type.toTypeName())
            }

            val overrideFunction = FunSpec.builder(function.simpleName.asString())
                .addModifiers(KModifier.OVERRIDE)
                .addParameters(parameterSpecs)
                .addStatement("val query = %N($entityParameterName)", queryFunc.name)
                .addStatement("%N(query)", "executeDml")
                .build()

            funSpecs.add(queryFunc)
            funSpecs.add(overrideFunction)
        }
    }
}