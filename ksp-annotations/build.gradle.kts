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

    implementation("org.ktorm:ktorm-core:3.6.0")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}