package dev.techullurgy.ksp

abstract class RoomDatabase {
    lateinit var databaseConfig: DatabaseConfig
    protected var isTransaction: Boolean = false

    fun isTransactionMode(): Boolean = isTransaction

    abstract fun setAsTransaction(isTransaction: Boolean)

    data class DatabaseConfig(
        val url: String,
        val username: String,
        val password: String
    )
}