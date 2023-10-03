package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.techullurgy.ksp.annotations.PrimaryKey
import dev.techullurgy.ksp.ext.removeSuffixInline
import dev.techullurgy.ksp.processor.extensions.*
import java.time.LocalDate
import java.time.LocalDateTime

internal data class ColumnDetails(
    val autoGenerate: Boolean = false,
    val propertyType: ClassName
)

object QueryBuilder {
    fun buildInsertQuery(name: String, entity: KSClassDeclaration): CodeBlock {
        val tableName = entity.getTableName()
        val columnNames = entity.getAllColumnNames()
        val allColumnDetails = entity.getAllColumnDetails()

        val filteredColumns = columnNames.filter { !allColumnDetails[it]!!.autoGenerate }

        val builder = StringBuilder("Insert into $tableName (")

        filteredColumns.forEach {
            builder.append("$it,")
        }
        builder.removeSuffixInline(",")
        builder.append(") VALUES ")

        val codeBlock = buildCodeBlock {
            add("return \"%L %L\"".fixSpaces(), builder.toString().fixSpaces(), insertTemplateString(name, entity, filteredColumns, allColumnDetails))
        }
        return codeBlock
    }

    fun buildUpdateQuery(name: String, entity: KSClassDeclaration): CodeBlock {
        val tableName = entity.getTableName()
        val columnNames = entity.getAllColumnNames()
        val allColumnDetails = entity.getAllColumnDetails()

        val primaryKeyColumn = columnNames.first { entity.getPropertyByColumnName(it).isAnnotatedWith(PrimaryKey::class) }

        fun getPrimaryKeyValue(): String {
            val property = entity.getPropertyByColumnName(primaryKeyColumn)
            val propertyType = property.getColumnDetails().propertyType
            return getDbValueString(name, property, propertyType)
        }

        val filteredColumns = columnNames.filter { it != primaryKeyColumn }

        val updateStatement = " Update $tableName set ".fixSpaces()
        val whereClause = " where $primaryKeyColumn = ${getPrimaryKeyValue()}".fixSpaces()
        val setColumns = updateTemplateString(name, entity, filteredColumns, allColumnDetails)

        return buildCodeBlock {
            add("return \"%L %L %L\"".fixSpaces(), updateStatement, setColumns.fixSpaces(), whereClause)
        }
    }

    private fun updateTemplateString(name: String, entity: KSClassDeclaration, filteredColumns: List<String>, allColumnDetails: Map<String, ColumnDetails>): String {
        val builder = StringBuilder()
        filteredColumns.forEach { columnName ->
            val property = entity.getPropertyByColumnName(columnName)
            val propertyType = allColumnDetails[columnName]!!.propertyType
            builder.append("$columnName = ${getDbValueString(name, property, propertyType)},")
        }
        builder.removeSuffixInline(",")
        return builder.toString()
    }

    private fun insertTemplateString(name: String, entity: KSClassDeclaration, filteredColumns: List<String>, allColumnDetails: Map<String, ColumnDetails>): String {
        val builder = StringBuilder()
        builder.append("(")
        filteredColumns.forEach { columnName ->
            val property = entity.getPropertyByColumnName(columnName)
            val propertyType = allColumnDetails[columnName]!!.propertyType
            builder.append(getDbValueString(name, property, propertyType) + ",")
        }
        builder.removeSuffixInline(",")
        return builder.append(")").toString()
    }

    private fun getDbValueString(variableName: String, property: KSPropertyDeclaration, propertyType: ClassName): String {
        return when(propertyType) {
            String::class.asClassName() -> {
                "'\${$variableName.${property.simpleName.asString()}}'"
            }
            Long::class.asClassName(),
            Int::class.asClassName() -> {
                "\${$variableName.${property.simpleName.asString()}}"
            }
            Boolean::class.asClassName() -> {
                "\${if($variableName.${property.simpleName.asString()}) 1 else 0}".fixSpaces()
            }
            LocalDateTime::class.asClassName() -> {
                "\${$variableName.${property.simpleName.asString()}.format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd hh:mm:ss\"))}".fixSpaces()
            }
            LocalDate::class.asClassName() -> {
                "\${$variableName.${property.simpleName.asString()}.format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd\"))}"
            }
            else -> {
                EntityDeclarations.findEntity(propertyType)?.let {
                    val property0 = it.getAllProperties().toList().findAnnotatedProperties(PrimaryKey::class).first()
                    require(property0.type.toTypeName() == Long::class.asTypeName() || property0.type.toTypeName() == Int::class.asTypeName()) { "Foreign Key Query Building Failed" }
                    "\${$variableName.${property.simpleName.asString()}.${property0.simpleName.asString()}}"
                } ?: EntityEnumDeclarations.findEnum(propertyType)?.let {
                        "'\${$variableName.${property.simpleName.asString()}.name}'"
                } ?: throw IllegalStateException("Property Types are undiscovered")
            }
        }
    }
}