package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

object TransactionFunctionBuilder {
    fun build(function: KSFunctionDeclaration, databaseReference: String = "this"): CodeBlock {
        return buildCodeBlock {
            addStatement("synchronized(this) {")
            addStatement("  try {")
            addStatement("      %N.setAsTransaction(true)", databaseReference)
            addStatement("      super.%N()", function.simpleName.asString())
            addStatement("  } catch (e: Exception) {")
            addStatement("      %N.setAsTransaction(false)", databaseReference)
            addStatement("      e.printStackTrace()")
            addStatement("  } finally { ")
            addStatement("      %N.setAsTransaction(false)", databaseReference)
            addStatement("  }")
            addStatement("}")
        }
    }
}