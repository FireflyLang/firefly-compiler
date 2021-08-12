package io.github.fireflylang.compiler.resolution

import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.jonathanxd.kores.base.TypeSpec
import com.github.jonathanxd.kores.common.MethodSpec

class MethodResolutionTable : ResolutionTableV2<MethodDeclaration>()
class MethodSignature(private val spec: MethodSpec) : Signature<MethodDeclaration> {
    override val name: String
        get() = this.spec.methodName

    override fun match(declaration: MethodDeclaration): Boolean =
        this.name == declaration.name && declaration.match(this.spec)

}