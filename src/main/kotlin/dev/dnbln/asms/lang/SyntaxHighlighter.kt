package dev.dnbln.asms.lang

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import dev.dnbln.asms.lang.psi.AsmElementTypes
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Default


class AsmSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = AsmLexerAdapter()
    companion object {
        private val ASM_OPERATORS = listOf(
            AsmElementTypes.PLUS,
            AsmElementTypes.MINUS,
            AsmElementTypes.COLON,
            AsmElementTypes.DOT,
            AsmElementTypes.PERCENT,
            AsmElementTypes.DOLLAR_SIGN
        )

        private val ASM_NUMS =
            listOf(AsmElementTypes.DEC_LITERAL, AsmElementTypes.HEX_LITERAL, AsmElementTypes.OCT_LITERAL)

        fun map(tokenType: IElementType?): AsmColor? =
            when (tokenType) {
                AsmElementTypes.ID -> AsmColor.IDENTIFIER
                AsmElementTypes.COMMENT -> AsmColor.COMMENT
                AsmElementTypes.COMMA -> AsmColor.COMMA
                AsmElementTypes.OPEN_PAREN, AsmElementTypes.CLOSE_PAREN -> AsmColor.PARENTHESES
                AsmElementTypes.STRING_LITERAL -> AsmColor.STRING
                TokenType.BAD_CHARACTER -> AsmColor.BAD_CHARACTER
                in ASM_NUMS -> AsmColor.NUMBER
                in ASM_OPERATORS -> AsmColor.OPERATOR
                else -> null
            }
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
        pack(map(tokenType)?.textAttributesKey)
}


enum class AsmColor(default: TextAttributesKey) {
    COMMENT(Default.LINE_COMMENT),
    COMMA(Default.COMMA),
    PARENTHESES(Default.PARENTHESES),
    OPERATOR(Default.OPERATION_SIGN),
    KEYWORD(Default.KEYWORD),
    NUMBER(Default.NUMBER),
    IDENTIFIER(Default.IDENTIFIER),
    BAD_CHARACTER(HighlighterColors.BAD_CHARACTER),
    STRING(Default.STRING),
    LABEL(Default.LABEL),
    DIRECTIVE(Default.KEYWORD);

    val textAttributesKey = createTextAttributesKey("ASM.${name}", default)
}