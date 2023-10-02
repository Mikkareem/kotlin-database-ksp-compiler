plugins {
    kotlin("jvm") version "1.9.10"
    id("com.google.devtools.ksp") version "1.9.10-1.0.13"
    application
}

group = "dev.techullurgy.ksp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ksp-annotations"))
    ksp(project(":ksp-compiler-processor"))

    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}