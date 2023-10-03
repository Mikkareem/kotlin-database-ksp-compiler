package dev.techullurgy.ksp

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class QueryExecutor(private val databaseConfig: RoomDatabase.DatabaseConfig) {

    private var transactionConnection: Connection? = null

    @Throws(SQLException::class)
    fun beginTransaction() {
        transactionConnection = getNewConnection()
        transactionConnection!!.autoCommit = false
    }

    @Throws(SQLException::class)
    fun endTransaction() {
        transactionConnection!!.commit()
        transactionConnection!!.close()
        transactionConnection = null
    }

    @Throws(SQLException::class)
    fun executeNonTransactionDML(dml: String) {
        try {
            val connection = getNewConnection()
            connection.use {
                it.execDml(dml)
            }
        } catch (e: SQLException) {
            throw e
        }
    }

    @Throws(SQLException::class)
    fun executeTransactionDML(dml: String) {
        try {
            transactionConnection?.execDml(dml)
        } catch (e: SQLException) {
            transactionConnection?.rollback()
            e.printStackTrace()
            throw e
        }
    }

    @Throws(SQLException::class)
    fun <T> executeTransactionSQL(sql: String, callback: (ResultSet) -> T): T {
        return try {
            transactionConnection!!.execSql(sql, callback)
        } catch (e: SQLException) {
            transactionConnection?.rollback()
            e.printStackTrace()
            throw e
        }
    }

    @Throws(SQLException::class)
    fun <T> executeNonTransactionSQL(sql: String, callback: (ResultSet) -> T): T {
        return try {
            val connection = getNewConnection()
            connection.use {
                it.execSql(sql, callback)
            }
        } catch (e: SQLException) {
            throw e
        }
    }

    @Throws(SQLException::class)
    private fun <T> Connection.execSql(sql: String, callback: (ResultSet) -> T): T {
        return createStatement().use {
            it.executeQuery(sql).use { resultSet ->
                callback(resultSet)
            }
        }
    }

    @Throws(SQLException::class)
    private fun Connection.execDml(dml: String) {
        createStatement().use {
            it.executeUpdate(dml)
        }
    }

    @Throws(SQLException::class)
    private fun getNewConnection(): Connection = DriverManager.getConnection(databaseConfig.url, databaseConfig.username, databaseConfig.password)
}