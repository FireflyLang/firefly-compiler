package io.github.fireflylang.compiler.inliner.visitor

import com.github.jonathanxd.iutils.`object`.TypedKey
import com.github.jonathanxd.iutils.kt.typeInfo

val VARIABLE_USAGE_COUNTER = TypedKey<MutableMap<String, Long>>("VARIABLE_USAGE_COUNTER", typeInfo())