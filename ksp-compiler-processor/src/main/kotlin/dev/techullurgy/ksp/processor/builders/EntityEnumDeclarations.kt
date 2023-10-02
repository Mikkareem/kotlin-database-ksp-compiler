package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

internal object EntityEnumDeclarations {
    private val enums: HashSet<KSClassDeclaration> = HashSet()

    fun add(klass: KSClassDeclaration) = enums.add(klass)

    fun findEnum(enumEntry: List<String>): KSClassDeclaration {
        val sortedEnumEntries = enumEntry.sorted()
        val result = enums.find { cl ->
            val d = cl.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .toList()

            val sortedStringRefs = d.map { it.simpleName.asString() }.sorted()
            sortedEnumEntries == sortedStringRefs
        }

        require(result != null) { "Enum Can't found" }
        return result
    }

    fun findEnum(enumClassName: ClassName): KSClassDeclaration? {
        return enums.find { it.simpleName.asString() == enumClassName.simpleName }
    }
}