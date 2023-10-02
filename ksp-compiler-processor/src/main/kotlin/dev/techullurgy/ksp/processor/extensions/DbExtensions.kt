package dev.techullurgy.ksp.processor.extensions

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.techullurgy.ksp.annotations.ColumnSpec
import dev.techullurgy.ksp.annotations.Entity
import dev.techullurgy.ksp.annotations.ForeignKey
import dev.techullurgy.ksp.annotations.PrimaryKey
import dev.techullurgy.ksp.processor.builders.ColumnDetails
import dev.techullurgy.ksp.processor.builders.EntityDeclarations
import dev.techullurgy.ksp.processor.builders.EntityEnumDeclarations
import java.time.LocalDate
import java.time.LocalDateTime

internal fun KSClassDeclaration.getAllColumnNames(): List<String> {
    val properties = getAllProperties()
    val dbColumnNames = properties.map { it.getColumnName() }
    return dbColumnNames.toList()
}

/** Used to build Insert/Update Statement
 *
 * @return {key - Column name}
 *     {value - Column Details like autoGenerate and type of value}
 */
internal fun KSClassDeclaration.getAllColumnDetails(): Map<String, ColumnDetails> {
    val result = HashMap<String, ColumnDetails>()

    val columnNames = getAllColumnNames()
    columnNames.forEach {
        val property = getPropertyByColumnName(it)
        val columnDetails = property.getColumnDetails()
        result[it] = columnDetails
    }

    return result
}

internal fun KSPropertyDeclaration.getColumnDetails(): ColumnDetails {
    val columnDetails = if(isAnnotatedWith(ForeignKey::class)) {
        ColumnDetails(propertyType = type.resolve().toClassName())
    } else {
        ColumnDetails(propertyType = getColumnType().getPropertyClassName())
    }
    return if(isAnnotatedWith(PrimaryKey::class)) {
        val autoGenerate: Boolean = getArgumentOfAnnotation(PrimaryKey::autoGenerate) as Boolean
        columnDetails.copy(autoGenerate = autoGenerate)
    } else columnDetails
}

internal fun KSClassDeclaration.getPropertyByColumnName(columnName: String): KSPropertyDeclaration {
    val properties = getAllProperties()
    return properties.firstOrNull {
        if(it.isAnnotatedWith(ColumnSpec::class)) {
            if((it.getArgumentOfAnnotation(ColumnSpec::columnName) as String) == columnName) true
            else it.simpleName.asString() == columnName
        } else it.simpleName.asString() == columnName
    } ?: throw RuntimeException("Can't find property by column name")
}

internal fun KSPropertyDeclaration.getColumnName(): String {
    return if(isAnnotatedWith(ColumnSpec::class)) {
        (getArgumentOfAnnotation(ColumnSpec::columnName) as String).ifEmpty { simpleName.asString() }
    } else simpleName.asString()
}

internal fun KSPropertyDeclaration.getColumnType(): String {
    var dbType = if(isAnnotatedWith(ColumnSpec::class)) {
        (getArgumentOfAnnotation(ColumnSpec::columnType) as String).ifEmpty {
            when(type.toTypeName().toString()) {
                "kotlin.String" -> "varchar(100)"
                "kotlin.Int" -> "int"
                "java.time.LocalDate" -> "date"
                "java.time.LocalDateTime" -> "datetime"
                "kotlin.Long" -> "bigint(20)"
                "kotlin.Boolean" -> "bool"
                else -> ""
            }
        }
    } else {
        when(type.toTypeName().toString()) {
            "kotlin.String" -> "varchar(100)"
            "kotlin.Int" -> "int"
            "java.time.LocalDate" -> "date"
            "java.time.LocalDateTime" -> "datetime"
            "kotlin.Long" -> "bigint(20)"
            "kotlin.Boolean" -> "bool"
            else -> ""
        }
    }

    if(dbType.isNotEmpty()) return dbType

    val declaration = type.resolve().declaration
    if(declaration.modifiers.contains(Modifier.ENUM)) {
        val classDeclaration = declaration as KSClassDeclaration
        EntityEnumDeclarations.add(classDeclaration)
        val values = classDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }
            .toList()
        dbType = values.asEnumString()
    }

    if(dbType.isNotEmpty()) return dbType

    if(declaration.isAnnotatedWith(Entity::class)) {
        val entityDeclaration = EntityDeclarations.findEntity(declaration.simpleName.asString())
        dbType = entityDeclaration.getAllProperties().toList().findAnnotatedProperties(PrimaryKey::class).first().getColumnType()
    }

    require(dbType.isNotEmpty()) {
        "DB Column Type can't inferred from ${type.toTypeName()}"
    }

    return dbType
}

internal fun KSClassDeclaration.getTableName(): String
        = (getArgumentOfAnnotation(Entity::tableName) as String).ifEmpty { simpleName.asString() }

internal fun String.getPropertyClassName(): ClassName {
    return if(contains("varchar")) String::class.asClassName()
        else if(contains("bigint")) Long::class.asClassName()
        else if(contains("int")) Int::class.asClassName()
        else if(contains("datetime")) LocalDateTime::class.asClassName()
        else if(contains("date")) LocalDate::class.asClassName()
        else if(contains("bool")) Boolean::class.asClassName()
        else if(contains("enum")) EntityEnumDeclarations.findEnum(toEnumEntries()).toClassName()
        else Unit::class.asClassName()
}

internal fun List<KSClassDeclaration>.asEnumString(): String {
    val quotedEnums = this.joinToString(",") { "'${it.simpleName.asString()}'" }.removeSuffix(",")
    return "enum($quotedEnums)"
}

internal fun String.toEnumEntries(): List<String> {
    require(startsWith("enum(") && endsWith(")") && length > 8) { "Not an Enum String" }

    val result = mutableListOf<String>()
    val entries = removePrefix("enum(").removeSuffix(")")
    val list = entries.split(",")
    list.forEach {
        result.add(it.removePrefix("'").removeSuffix("'"))
    }

    return result.toList()
}