package io.github.fireflylang.compiler.parser

import io.github.fireflylang.compiler.errors.ErrorReport
import io.github.fireflylang.compiler.resolution.ResolutionMedium
import io.github.fireflylang.compiler.resolution.ResolutionTables
import io.github.fireflylang.compiler.resolution.TypeResolutionMedium

data class ParseContext(
    val resolutionTables: ResolutionTables = ResolutionTables(ResolutionMedium(), TypeResolutionMedium()),
    val errorReport: ErrorReport = ErrorReport()
)