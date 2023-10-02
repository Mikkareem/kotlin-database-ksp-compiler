package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import dev.techullurgy.ksp.builders.Structure
import dev.techullurgy.ksp.builders.TableInitiation
import java.util.*

object TableInitiationBuilder {
    fun build(codeGenerator: CodeGenerator, dependencies: Dependencies, structure: Structure) {
        val tableInitiationInterface = TableInitiation::class.asTypeName()
        val newClassName = ClassName(
            structure.packageName,
            structure.tableName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                + "TableInitiation"
        )

        val overrideValues = TableInitiation::class.members.filter { it.isAbstract }

        val overrideProperties = overrideValues.map {
            PropertySpec.builder(it.name, it.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .build()
        }

        val tableNameGetterSpec = FunSpec.getterBuilder().addStatement("return %S", structure.tableName).build()
        val tableStructureGetterSpec = FunSpec.getterBuilder().addStatement("return %L", structure.tableName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + "Structure").build()

        val tableNameProperty = overrideProperties.first { it.name == TableInitiation::tableName.name }.toBuilder().getter(tableNameGetterSpec).build()
        val tableStructureProperty = overrideProperties.first { it.name == TableInitiation::tableStructure.name }.toBuilder().getter(tableStructureGetterSpec).build()

        val fileSpec = FileSpec.builder(newClassName)
            .addType(
                TypeSpec.objectBuilder(newClassName)
                    .addModifiers(KModifier.INTERNAL)
                    .addSuperinterface(tableInitiationInterface)
                    .addProperty(tableNameProperty)
                    .addProperty(tableStructureProperty)
                    .build()
            ).build()

        fileSpec.writeTo(codeGenerator, dependencies)
    }
}