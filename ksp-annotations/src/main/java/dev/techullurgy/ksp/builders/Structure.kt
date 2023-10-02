package dev.techullurgy.ksp.builders

import com.google.devtools.ksp.symbol.KSPropertyDeclaration

data class Structure(
    val tableName: String,
    val packageName: String,
    val columnSpecs: List<Column>
) {
    data class Column(
        val columnName: String,
        val dbColumnType: String,
        val property: KSPropertyDeclaration,
        val foreignKey: ForeignKey? = null
    )
}
