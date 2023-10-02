package dev.techullurgy.ksp.builders

interface TableStructure {
    val typeMap: Map<String, String>
    val foreignKeys: List<ForeignKey>?
}

data class ForeignKey(
    val columnName: String,
    val references: References
) {
    data class References(
        val tableName: String,
        val columnName: String
    )
}