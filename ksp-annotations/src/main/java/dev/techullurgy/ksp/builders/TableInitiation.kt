package dev.techullurgy.ksp.builders

import dev.techullurgy.ksp.ext.createTable
import dev.techullurgy.ksp.ext.isTableExists
import org.ktorm.database.Database

interface TableInitiation {
    val tableName: String
    val tableStructure: TableStructure
    suspend fun initiate(database: Database): Boolean
            = if(!database.isTableExists(tableName)) createTable(database, printSql = true) else true
}