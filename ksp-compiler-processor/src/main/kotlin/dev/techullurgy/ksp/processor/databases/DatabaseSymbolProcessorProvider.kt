package dev.techullurgy.ksp.processor.databases

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DatabaseSymbolProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DatabaseSymbolProcessor(
            codeGenerator = environment.codeGenerator
        )
    }
}