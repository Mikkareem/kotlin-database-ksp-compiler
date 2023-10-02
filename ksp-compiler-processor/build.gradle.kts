plugins {
    kotlin("jvm") version "1.9.10"
}

group = "dev.techullurgy.ksp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.10-1.0.13")
    implementation("com.squareup:kotlinpoet:1.14.2")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")

    implementation(project(":ksp-annotations"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}