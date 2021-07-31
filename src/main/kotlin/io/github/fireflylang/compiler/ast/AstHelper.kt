/**
 *      firefly-compiler - Firefly Language parser and compiler.
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) JonathanxD <https://github.com/JonathanxD/>
 *      Copyright (c) contributors
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package io.github.fireflylang.compiler.ast

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.iutils.kt.set
import com.github.jonathanxd.kores.MutableInstructions
import com.github.jonathanxd.kores.Types
import com.github.jonathanxd.kores.base.*
import com.github.jonathanxd.kores.type.typeOf
import io.github.fireflylang.compiler.*

fun createTypeDeclaration(unit: FireflyUnit, data: TypedData): TypeDeclaration =
    if (unit.unitType == UnitType.UNIT) {
        ClassDeclaration.Builder.builder()
            .qualifiedName(unit.qualifiedName)
            .modifiers(KoresModifier.PUBLIC, KoresModifier.FINAL)
            .constructors(createConstructors(unit, data))
            .methods(createMethods(unit, data))
            .build()
    } else {
        throw UnsupportedOperationException("Unit type ${unit.unitType} not supported yet.")
    }

fun createConstructors(unit: FireflyUnit, data: TypedData): List<ConstructorDeclaration> =
    if (unit.unitType == UnitType.UNIT) {
        listOf(
            ConstructorDeclaration.Builder.builder()
                .modifiers(KoresModifier.PRIVATE)
                .build()
        )
    } else {
        emptyList()
    }

fun createMethods(unit: FireflyUnit, data: TypedData): List<MethodDeclaration> =
    if (unit.unitType == UnitType.UNIT) {
        listOf(createEntrypointMethod(unit, data))
    } else {
        emptyList()
    }

private fun createEntrypointMethod(unit: FireflyUnit, data: TypedData): MethodDeclaration =
    MethodDeclaration.Builder.builder()
        .name("entrypoint")
        .parameters(createEntrypointMethodParameters(unit, data))
        .modifiers(KoresModifier.PUBLIC, KoresModifier.STATIC)
        .returnType(Types.VOID)
        .body(MutableInstructions.create())
        .build().also {
            data[ENTRYPOINT_METHOD] = it
            data[ENTRYPOINT_METHOD_SOURCE] = it.body as MutableInstructions
            data[CURRENT_SOURCE] = it.body as MutableInstructions
        }

private fun createEntrypointMethodParameters(unit: FireflyUnit, data: TypedData): List<KoresParameter> =
    if (unit.unitType == UnitType.UNIT) {
        listOf(
            KoresParameter.Builder.builder()
                .modifiers(KoresModifier.FINAL)
                .type(typeOf<Array<String>>())
                .name("args")
                .build()
        )
    } else {
        emptyList()
    }