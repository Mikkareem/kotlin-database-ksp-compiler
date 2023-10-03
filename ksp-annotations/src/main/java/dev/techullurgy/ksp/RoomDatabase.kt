package dev.techullurgy.ksp

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

abstract class RoomDatabase {
    lateinit var databaseConfig: DatabaseConfig

    data class DatabaseConfig(
        val url: String,
        val username: String,
        val password: String
    )
}