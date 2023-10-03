package dev.techullurgy.ksp

import kotlin.reflect.KClass

interface Room {
    companion object {
        fun databaseBuilder(databaseConfig: RoomDatabase.DatabaseConfig): Builder {
            return Builder(databaseConfig)
        }
    }

    class Builder internal constructor(private val databaseConfig: RoomDatabase.DatabaseConfig){
        private lateinit var roomDatabase: KClass<out RoomDatabase>
        fun build(): RoomDatabase {
            require(::roomDatabase.isInitialized) { "Database can't be initialised" }
            return (Class.forName(roomDatabase.qualifiedName + "Impl").getDeclaredConstructor().newInstance() as RoomDatabase).apply {
                databaseConfig = this@Builder.databaseConfig
            }
        }

        fun getInstanceOf(klass: KClass<out RoomDatabase>): Builder {
            roomDatabase = klass
            return this
        }
    }
}