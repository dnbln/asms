package dev.dnbln.asms.lang

import com.intellij.psi.tree.IElementType
import dev.dnbln.asms.lang.AsmLang

class AsmTokenType(name: String): IElementType(name, AsmLang) {
    override fun toString(): String = "AsmTokenType." + super.toString()
}