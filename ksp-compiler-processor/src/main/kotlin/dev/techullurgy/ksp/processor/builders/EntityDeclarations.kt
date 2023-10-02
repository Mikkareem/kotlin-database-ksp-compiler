package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

internal object EntityDeclarations {
    private val entities: HashSet<KSClassDeclaration> = HashSet()

    fun add(klass: KSClassDeclaration) = entities.add(klass)

    fun findEntity(entity: String): KSClassDeclaration {
        val result = entities.find { it.simpleName.asString() == entity }
        require(result != null) { "Entity can't found $entity $entities" }
        return result
    }

    fun findEntity(entity: ClassName): KSClassDeclaration? {
        return entities.find { it.simpleName.asString() == entity.simpleName }
    }
}