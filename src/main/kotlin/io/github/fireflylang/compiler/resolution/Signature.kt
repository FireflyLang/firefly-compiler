package io.github.fireflylang.compiler.resolution

interface Signature<T> {
    val name: String

    fun match(declaration: T): Boolean
}