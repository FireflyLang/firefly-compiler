package io.github.fireflylang.compiler.test.snippets

import io.github.fireflylang.compiler.FireflyUnit
import io.github.fireflylang.compiler.UnitType
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import java.nio.file.Paths

val simpleFn =
    createUnit(
        fileName = "SimpleFn.firefly",
        contentStream = {
            CharStreams.fromString(
                """
                fn hello() {
                    println("Hello world")
                }
                fn greet(text: String) {
                    println("Hello world")
                }
                fn greet2(text: java.lang.String) {
                    println("Hello world")
                }
                fn greet3(text: `java.lang.String:a`) {
                    println("Hello world")
                }
            """.trimIndent()
            )
        }
    )

val simpleFnWithDefault =
    createUnit(
        fileName = "SimpleFn.firefly",
        contentStream = {
            CharStreams.fromString(
                """
                fn hello() {
                    println("Hello world")
                }
                fn greet(text: String) {
                    println("Hello world")
                }
                fn greet2(text: java.lang.String = "Hello world") {
                    println("Hello world")
                }
                fn greet3(text: `java.lang.String:a`) {
                    println("Hello world")
                }
            """.trimIndent()
            )
        }
    )

