package dev.techullurgy.ksp.processor.builders

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

object DatabaseBuilder {
    fun build(codeGenerator: CodeGenerator, dec: KSClassDeclaration, funSpecs: List<FunSpec>, propertySpecs: List<PropertySpec>) {
        val newFileClassName = ClassName(dec.packageName.asString(), dec.simpleName.asString() + "Impl")
        val typeSpec = TypeSpec.classBuilder(newFileClassName).addProperties(propertySpecs)

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