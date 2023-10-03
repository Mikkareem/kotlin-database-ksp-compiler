package dev.techullurgy.ksp

abstract class RoomDatabase {
    lateinit var databaseConfig: DatabaseConfig

    data class DatabaseConfig(
        val url: String,
        val username: String,
        val password: String
    )
}