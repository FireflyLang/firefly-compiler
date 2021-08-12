package io.github.fireflylang.compiler.resolution

import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.base.TypeDeclaration
import com.github.jonathanxd.kores.base.TypeSpec
import com.github.jonathanxd.kores.common.MethodSpec

class TypeResolutionTable : ResolutionTableV2<TypeDeclaration>()
class TypeSignature(private val typeName: String) : Signature<TypeDeclaration> {
    override val name: String
        get() = this.typeName

    override fun match(declaration: TypeDeclaration): Boolean =
        this.typeName == declaration.canonicalName

}