package dev.techullurgy.ksp.processor.daos

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DaosSymbolProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DaosSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}