package dev.dnbln.asms.lang

import com.intellij.lexer.FlexAdapter
import dev.dnbln.asms.lang.lexer._AsmLexer

class AsmLexerAdapter: FlexAdapter(_AsmLexer(null))