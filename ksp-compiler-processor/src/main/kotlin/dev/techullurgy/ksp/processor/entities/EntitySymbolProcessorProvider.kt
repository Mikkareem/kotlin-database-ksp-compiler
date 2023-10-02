package dev.techullurgy.ksp.processor.entities

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class EntitySymbolProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EntitySymbolProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}