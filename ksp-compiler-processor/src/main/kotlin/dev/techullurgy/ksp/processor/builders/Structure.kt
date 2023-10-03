package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import dev.techullurgy.ksp.builders.ForeignKey

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
