package dev.dnbln.asms.lang.psi

val AsmNum.value: Int
    get() = decLit?.value ?: hexLit?.value ?: octLit?.value ?: error("Unknown asm num value")

val AsmDecLit.value: Int
    get() = decLiteral.text.toInt()

val AsmOctLit.value: Int
    get() = octLiteral.text.toInt(8)

val AsmHexLit.value: Int
    get() = hexLiteral.text.toInt(16)
