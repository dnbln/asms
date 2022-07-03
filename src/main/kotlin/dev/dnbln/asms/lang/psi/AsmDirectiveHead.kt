package dev.dnbln.asms.lang.psi

val AsmDirectiveHead.directiveName: String
    get() = (decLiteral?.text ?: "") + id.text