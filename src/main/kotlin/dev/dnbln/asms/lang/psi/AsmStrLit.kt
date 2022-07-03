package dev.dnbln.asms.lang.psi

val AsmStrLit.strLitInnerText: String
    get() = text.substring(1, text.length - 1)