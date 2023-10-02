package dev.techullurgy.ksp.ext

import dev.techullurgy.ksp.builders.TableInitiation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.Query
import java.sql.SQLException
import kotlin.system.exitProcess

internal suspend inline fun <R> Database.executeQuery(
    sql: String,
    crossinline onSuccess: () -> R,
    crossinline onFailure: () -> R,
    isDDL: Boolean = false
): R = withContext(Dispatchers.IO) {
    useConnection { conn ->
        try {
            conn.createStatement().use {
                if(!isDDL) {
                    it.executeQuery(sql).close()
                } else {
                    it.executeUpdate(sql)
                }
                onSuccess()
            }
        } catch (e: SQLException) {
            println("Error: ${e.errorCode} ---- ${e.localizedMessage}")
            if(isDDL) {
                e.printStackTrace()
                exitProcess(123)
            }
            onFailure()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure()
        }
    }
}

internal suspend inline fun Database.isTableExists(tableName: String): Boolean
        = executeQuery(
    sql = "select 1 from $tableName",
    onSuccess = { true },
    onFailure = { false }
)

internal suspend fun TableInitiation.createTable(database: Database, printSql: Boolean = false): Boolean = withContext(Dispatchers.IO) {
    val sqlBuilder = StringBuilder()
    sqlBuilder.append("CREATE TABLE ")
    sqlBuilder.append(tableName)
    sqlBuilder.append(" ( ")

    tableStructure.typeMap.forEach {
        sqlBuilder.append(it.key)
        sqlBuilder.append(" ")
        sqlBuilder.append(it.value)
        sqlBuilder.append(", ")
    }

    tableStructure.foreignKeys?.let {
        it.forEach { key ->
            sqlBuilder.append(" FOREIGN KEY (${key.columnName}) ")
            sqlBuilder.append("references ${key.references.tableName}")
            sqlBuilder.append("(${key.references.columnName}), ")
        }
        sqlBuilder.removeSuffixInline(", ")
    } ?: sqlBuilder.removeSuffixInline(", ")

    sqlBuilder.append(" ) ")

    val sql = sqlBuilder.toString()
    if (printSql) println(sql)

    database.executeQuery(
        sql = sql,
        isDDL = true,
        onSuccess = { true },
        onFailure = { false }
    )
}

fun StringBuilder.removeSuffixInline(suffix: String) {
    val temp = removeSuffix(suffix)
    clear()
    append(temp)
}

internal fun Query.hasRecords(): Boolean = totalRecordsInAllPages > 0