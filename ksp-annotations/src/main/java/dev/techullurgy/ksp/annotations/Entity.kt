package dev.techullurgy.ksp.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Entity(
    val tableName: String
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class ColumnSpec(
    val columnName: String = "",
    val columnType: String = ""
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class PrimaryKey(
    val autoGenerate: Boolean = true
)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class ForeignKey

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Dao

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Insert

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Update

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Query

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class DML

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Database