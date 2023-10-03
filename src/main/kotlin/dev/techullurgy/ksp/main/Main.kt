package dev.techullurgy.ksp.main

import dev.techullurgy.ksp.Room
import dev.techullurgy.ksp.RoomDatabase
import dev.techullurgy.ksp.annotations.*
import dev.techullurgy.ksp.main.test.Reft
import java.time.LocalDateTime

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
    fun insertAllRoyals(royal1: RoyalEntity)

    @Update
    fun updateAllRoyals(royal2: RoyalEntity)
}

@Database
abstract class MyDatabase: RoomDatabase() {
    abstract fun timerDao(): TimerDao
    abstract fun royalDao(): RoyalDao

    companion object {
        fun getDatabase(): MyDatabase {
            val databaseConfig = DatabaseConfig("", "", "")
            return Room.databaseBuilder(databaseConfig).getInstanceOf(MyDatabase::class).build() as MyDatabase
        }
    }
}

fun main() {
    MyDatabase.getDatabase().timerDao()
}


/**
 * abstract class RoomDatabase {
 *      companion object {
 *          fun builder(databaseConfig: DatabaseConfig) {
 *              return MydatabaseImpl(databaseConfig)
 *          }
 *      }
 * }
 *
 * class MyDatabaseImpl(private val databaseConfig: DatabaseConfig) {
 *      val queryExecutor: QueryExecutor = QueryExecutor(databaseConfig)
 *
 *      private val timerDao: TimerDao = TimerDaoImplementation(queryExecutor)
 *      private val royalDao: RoyalDao = RoyalDaoImplementation(queryExecutor)
 *
 *      override fun timerDao(): TimerDao = timerDao
 *      override fun royalDao(): RoyalDao = royalDao
 * }
 *
 * class TimerDaoImplementation(queryExecutor: QueryExecutor): TimerDao {
 *      private val isTransaction: Boolean = false
 *
 *      private fun setAsTransaction(isTransaction: Boolean) {
 *          this.isTransaction = isTransaction
 *          if(this.isTransaction) {
 *              queryExecutor.beginTransaction()
 *          } else {
 *              queryExecutor.endTransaction()
 *          }
 *      }
 *
 *      override fun insert(entity: TimerEntity) {
 *          val statement = ...(entity)
 *          execute(statement)
 *      }
 *      override fun update(entity: TimerEntity) {....}
 *
 *      private fun execute(dml: String) {
 *          if(isTransaction) {
 *              queryExecutor.executeTransaction(dml)
 *          } else {
 *              queryExecutor.executeNonTransaction(dml)
 *          }
 *      }
 *
 *      override fun transactionFunc() {
 *          synchronized(this) {
 *              try{
    *              setAsTransaction(true)
 *  *              super.transactionFunc()
 *  *          } catch(e: Exception) {
 *  *              setAsTransaction(false)
 *  *              e.printStackTrace()
 *  *          } finally {
 *  *              setAsTransaction(false)
 *  *          }
 *          }
 *      }
 * }
 *
 * class QueryExecutor(databaseConfig: DatabaseConfig) {
 *      private val transactionConnection: Connection? = null
 *
 *      fun executeNonTransaction(dml: String) {
 *          var connection = DriverManager.getConnection(databaseConfig)
 *          try { ... } catch(e: SQLException) {} finally {connection.close()}
 *      }
 *
 *      fun executeTransaction(dml: String) {
 *          try {
 *              // Execute queries and NO COMMIT
 *          } catch(e: Exception) {
 *              transactionConnection.rollback()
 *              e.printStackTrace()
 *              throw e
 *          }
 *      }
 *
 *      fun beginTransaction() {
 *          transactionConnection = DriverManager.getConnection(databaseConfig)
 *          transactionConnection.autoCommit = false
 *      }
 *      fun endTransaction() {
 *         try { transactionConnection.commit() } catch(e: Exception){} finally { // close and transactionConnection = null }
 *      }
 * }
 */

