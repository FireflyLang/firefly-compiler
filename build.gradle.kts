import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    id("com.github.hierynomus.license") version "0.16.1"
    id("antlr")
}

group = "io.github.fireflylang"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Lexer and Parser generator
    antlr("org.antlr:antlr4:4.9.2")
    // AST & Compilation
    implementation("com.github.koresframework.Kores:Kores:4.0.2.base")
    implementation("com.github.koresframework.Kores:Kores-DSL:4.0.2.base")
    implementation("com.github.koresframework.Kores-BytecodeWriter:Kores-BytecodeWriter:4.0.1.bytecode.1")

    // Processing Utils
    implementation("com.github.JonathanxD.JwIUtils:JwIUtils:4.17.2")
    implementation("com.github.JonathanxD.JwIUtils:jwiutils-kt:4.17.2")
    implementation("com.github.JonathanxD.JwIUtils:specializations:4.17.2")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.10")
    //implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha2")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:4.6.1")
    testImplementation("io.kotest:kotest-assertions-core:4.6.1")
    testImplementation("io.kotest:kotest-property:4.6.1")
    // Kotlin Test
    testImplementation(kotlin("test"))
}

tasks.test {
    // JUnit 5
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "13"
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-package", "io.github.fireflylang.compiler.grammar")
    finalizedBy("licenseFormat")
}

tasks.create<Exec>("grun") {
    dependsOn("generateGrammarSource")
    dependsOn("build")
    this.workingDir(file("$buildDir/classes/java/main"))
    this.commandLine("grun", "io.github.fireflylang.compiler.grammar.FireflyLang", "unit", "-gui", "$projectDir/test.firefly")
}

tasks.withType<nl.javadude.gradle.plugins.license.License> {
    header = rootProject.file("LICENSE_HEADER")
    strictCheck = true
    mapping("g4", "JAVADOC_STYLE")
    //exclude { !it.path.startsWith("$buildDir/generated-src") }
}