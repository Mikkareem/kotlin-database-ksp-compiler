package dev.techullurgy.ksp.processor.entities

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import dev.techullurgy.ksp.annotations.Entity
import dev.techullurgy.ksp.builders.Structure
import dev.techullurgy.ksp.processor.builders.EntityDeclarations
import dev.techullurgy.ksp.processor.builders.TableInitiationBuilder
import dev.techullurgy.ksp.processor.builders.TableStructureBuilder
import dev.techullurgy.ksp.processor.extensions.*
import dev.techullurgy.ksp.processor.extensions.getColumnName
import dev.techullurgy.ksp.processor.extensions.getTableName

class EntitySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {
    private var tableName: String = ""
    private var listOfColumns: MutableList<Structure.Column> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Entity::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>().toList()

        val entityCollection = EntitiesCollection().apply { addAll(symbols) }

        while(entityCollection.hasNext()) {
            val it = entityCollection.next()!!
            it.accept(EntityVisitor(), Unit)
            if(it.getAllProperties().toList().size == listOfColumns.size) {
                val packageName = it.packageName.asString()
                val structure = Structure(
                    tableName = tableName,
                    packageName = packageName,
                    columnSpecs = listOfColumns
                )
                val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())
                TableStructureBuilder.build(codeGenerator, dependencies, structure)
                TableInitiationBuilder.build(codeGenerator, dependencies, structure)
            }
            clearAll()
        }
        return emptyList()
    }

    private fun clearAll() {
        tableName = ""
        listOfColumns = mutableListOf()
    }

    private inner class EntityVisitor: KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if(!classDeclaration.modifiers.contains(Modifier.DATA)) {
                logger.error("@Entity is allowed only for Data Classes")
                return
            }

            EntityDeclarations.add(classDeclaration)

            tableName = classDeclaration.getTableName()

            val properties = classDeclaration.getAllProperties().toList()

            properties.forEach {
                it.accept(this, Unit)
            }

//            EntityDeclarations.add(classDeclaration)
        }

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            val dbType = property.getColumnType()
            val columnName = property.getColumnName()

            listOfColumns.add(
                Structure.Column(
                    columnName = columnName,
                    dbColumnType = dbType,
                    property = property
                )
            )
        }
    }
}