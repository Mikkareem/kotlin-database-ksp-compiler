package dev.techullurgy.ksp.processor.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.techullurgy.ksp.annotations.ForeignKey

class EntitiesCollection {
    private val entities: MutableSet<KSClassDeclaration> = mutableSetOf()
    private val visitedEntities: MutableSet<KSClassDeclaration> = mutableSetOf()
    private val adjacencyList: HashMap<String, MutableList<KSClassDeclaration>> = HashMap()
    fun addAll(klasses: List<KSClassDeclaration>) {
        entities.addAll(klasses)
        buildGraph()
    }

    private fun buildGraph() {
        for(entity in entities) {
            if(adjacencyList[entity.simpleName.asString()] == null) {
                adjacencyList[entity.simpleName.asString()] = mutableListOf()
            }

            val foreignKeyProp = entity.getAllProperties().toList().findAnnotatedProperties(ForeignKey::class)
            if(foreignKeyProp.isNotEmpty()) {
                foreignKeyProp.forEach {
                    adjacencyList[entity.simpleName.asString()]!!.add(it.type.resolve().declaration as KSClassDeclaration)
                }
            }
        }
    }

    fun hasNext(): Boolean {
        for(entity in entities) {
            val dependencies = adjacencyList[entity.simpleName.asString()]!!
            if(dependencies.isEmpty() && !visitedEntities.contains(entity)) {
                return true
            }

            var visited = true
            for(dependency in dependencies) {
                if(!visitedEntities.contains(dependency)) {
                    visited = false
                    break
                }
            }

            if(visited && !visitedEntities.contains(entity)) {
                return true
            }
        }
        return false
    }

    fun next(): KSClassDeclaration? {
        for(entity in entities) {
            val dependencies = adjacencyList[entity.simpleName.asString()]!!
            if(dependencies.isEmpty() && !visitedEntities.contains(entity)) {
                visitedEntities.add(entity)
                return entity
            }

            var visited = true
            for(dependency in dependencies) {
                if(!visitedEntities.contains(dependency)) {
                    visited = false
                    break
                }
            }

            if(visited && !visitedEntities.contains(entity)) {
                visitedEntities.add(entity)
                return entity
            }
        }
        return null
    }
}