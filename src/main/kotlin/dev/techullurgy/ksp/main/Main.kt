package dev.techullurgy.ksp.main

import dev.techullurgy.ksp.annotations.*
import dev.techullurgy.ksp.main.test.Reft
import java.time.LocalDate
import java.time.LocalDateTime

fun main() = Unit

@Entity(tableName = "timer")
data class TimerEntity(
    @PrimaryKey
    val id: Long,
    val time: LocalDateTime,
    @ForeignKey
    val royal: RoyalEntity,
    val spacer: Boolean,
    val reft: Reft
)

@Entity(tableName = "royal")
data class RoyalEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val kName: String,
    @ColumnSpec(columnName = "office")
    val kOffice: Long,
    val kVariance: String,
    val reft: Reft
)

@Dao
interface TimerDao {
    @Insert
    fun insertAllTimes(timer1: TimerEntity)

    @Update
    fun updateAllTimes(timer2: TimerEntity)
}

@Dao
interface RoyalDao {
    @Insert
    fun insertAllRoyals(timer1: RoyalEntity)

    @Update
    fun updateAllRoyals(timer2: RoyalEntity)
}

@Database
abstract class MyDatabase {
    abstract fun timerDao(): TimerDao
    abstract fun royalDao(): RoyalDao

    companion object {
        fun getDatabase(): String {
            return ""
        }
    }
}