package dev.techullurgy.ksp.processor.extensions

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


internal fun KSClassDeclaration.getCompanionFunctions(): List<KSFunctionDeclaration> {
    return declarations
        .filterIsInstance<KSClassDeclaration>()
        .first { it.isCompanionObject }
        .getDeclaredFunctions()
        .filter { !it.isConstructor() }
        .toList()
}

internal fun KSClassDeclaration.getAllAbstractFunctions(): List<KSFunctionDeclaration> {
    return getAllFunctions().toList().filter { it.isAbstract }
}

internal fun KSFunctionDeclaration.getOverridableFunSpecBuilder(): FunSpec.Builder {
    return FunSpec.builder(simpleName.asString())
        .addModifiers(KModifier.OVERRIDE)
        .returns(returnType!!.toTypeName())
}

internal inline fun <reified T: Annotation> KSDeclaration.getArgumentOfAnnotation(prop: KProperty1<T, *>): Any?
        = getAllArgumentsOfAnnotation(T::class).first { it.name?.asString() == prop.name }.value

internal fun <T: Annotation> KSDeclaration.isAnnotatedWith(klass: KClass<T>): Boolean
        = annotations.any { ann -> ann.shortName.asString() == klass.simpleName }

internal fun <T: Annotation> KSDeclaration.getAllArgumentsOfAnnotation(klass: KClass<T>): List<KSValueArgument>
        = annotations.first { it.shortName.asString() == klass.simpleName }.arguments


internal fun <T: Annotation> KSClassDeclaration.getAllAbstractFunctionsOfAnnotated(annotationKlass: KClass<T>): List<KSFunctionDeclaration> {
    return getAllAbstractFunctions().filter { it.isAnnotatedWith(annotationKlass) }
}

internal fun <T: Annotation> KSClassDeclaration.getAllFunctionsOfAnnotated(annotationKlass: KClass<T>): List<KSFunctionDeclaration> {
    return getAllFunctions().toList().filter { it.isAnnotatedWith(annotationKlass) }
}

internal fun <T: Annotation> KSClassDeclaration.getAllAbstractFunctionsOfReturnTypeAnnotatedWith(annotationKlass: KClass<T>): List<KSFunctionDeclaration> {
    return getAllAbstractFunctions().filter {
        it.returnType!!.resolve().declaration.isAnnotatedWith(annotationKlass)
    }
}

internal fun <T: Annotation> List<KSPropertyDeclaration>.findAnnotatedProperties(klass: KClass<T>): List<KSPropertyDeclaration> = filter { it.isAnnotatedWith(klass) }
