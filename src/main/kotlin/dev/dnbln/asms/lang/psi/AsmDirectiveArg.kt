package dev.dnbln.asms.lang.psi

val AsmDirectiveArg.kind: String
    get() = if (directiveArgName != null) "name"
    else if (num != null) "num"
    else if (strLit != null) "string literal"
    else error("unknown directive arg kind")