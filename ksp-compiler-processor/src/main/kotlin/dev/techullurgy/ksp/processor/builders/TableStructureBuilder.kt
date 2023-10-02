package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import dev.techullurgy.ksp.annotations.PrimaryKey
import dev.techullurgy.ksp.builders.Structure
import dev.techullurgy.ksp.builders.TableStructure
import java.util.*

internal object TableStructureBuilder {
    fun build(codeGenerator: CodeGenerator, dependencies: Dependencies, structure: Structure) {
        val tableStructureInterface = TableStructure::class.asTypeName()
        val newClassName = ClassName(
            structure.packageName,
            structure.tableName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    + "Structure"
        )

        val propertySpecs = structure.columnSpecs.map {
            PropertySpec.builder(it.property.simpleName.asString().uppercase(), String::class.asTypeName())
                .addModifiers(KModifier.CONST)
                .initializer("%S", it.columnName)
                .build()
        }

        val overrideValues = TableStructure::class.members.filter { it.isAbstract }

        val overrideProperties = overrideValues.map {
            PropertySpec.builder(it.name, it.returnType.asTypeName())
                .addModifiers(KModifier.OVERRIDE)
                .build()
        }

        val typeMapGetterFunc = FunSpec.getterBuilder()
            .addCode("return HashMap<String, String>().apply { \n")

        structure.columnSpecs.forEach {
            val columnName = it.property.simpleName.asString().uppercase()
            val dbType = it.dbColumnType

            val primaryKeySequence = it.property.annotations.filter { ann -> ann.shortName.asString() == PrimaryKey::class.simpleName }
            val isPrimaryKey = !primaryKeySequence.none()
            val isAutogenerate = if(isPrimaryKey) {
                primaryKeySequence.first().arguments.first { arg -> arg.name?.asString() == PrimaryKey::autoGenerate.name }.value as Boolean
            } else false

            val valueString: String = getValueString(dbType, isPrimaryKey, isAutogenerate)

            typeMapGetterFunc.addCode("    put($columnName, \"${valueString}\") \n")
        }

        typeMapGetterFunc.addCode("}")

        val typeMapProperty = overrideProperties.first { it.name == TableStructure::typeMap.name }.toBuilder()
            .getter(
                typeMapGetterFunc.build()
            )
            .build()

        val foreignKeyProperty = overrideProperties.first { it.name == TableStructure::foreignKeys.name }.toBuilder()
            .getter(
                FunSpec.getterBuilder().addStatement("return null").build()
            )
            .build()

        val fileSpec = FileSpec.builder(newClassName).addType(
            TypeSpec.objectBuilder(newClassName)
                .addModifiers(KModifier.INTERNAL)
                .addSuperinterface(tableStructureInterface)
                .addProperties(propertySpecs)
                .addProperty(typeMapProperty)
                .addProperty(foreignKeyProperty)
                .build()
        ).build()

        fileSpec.writeTo(codeGenerator, dependencies)
    }

    private fun getValueString(dbType: String, isPrimaryKey: Boolean, isAutogenerate: Boolean): String {
        return "$dbType ${if(isAutogenerate) "auto_increment" else ""} ${if(isPrimaryKey) "primary key" else ""} "
    }
}