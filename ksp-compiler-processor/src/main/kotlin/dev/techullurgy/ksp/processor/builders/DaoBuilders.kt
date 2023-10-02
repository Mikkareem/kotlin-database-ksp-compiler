package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

object DaoBuilders {
    fun build(codeGenerator: CodeGenerator, dec: KSClassDeclaration, funSpecs: List<FunSpec>) {
        val newFileClassName = ClassName(dec.packageName.asString(), dec.simpleName.asString() + "Implementation")
        val typeSpec = TypeSpec.classBuilder(newFileClassName)

        if(dec.modifiers.contains(Modifier.ABSTRACT)) {
            typeSpec.superclass(dec.toClassName())
        } else if(dec.classKind == ClassKind.INTERFACE) {
            typeSpec.addSuperinterface(dec.toClassName())
        }

        val fileSpec = FileSpec.builder(newFileClassName)
            .addType(typeSpec.addFunctions(funSpecs).build())
            .build()

        fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
    }
}