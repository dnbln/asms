package dev.dnbln.asms.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.dnbln.asms.lang.AsmLang
import dev.dnbln.asms.lang.AsmLexerAdapter
import dev.dnbln.asms.lang.psi.AsmElementTypes
import dev.dnbln.asms.lang.psi.AsmFile

class AsmParserDefinition : ParserDefinition {
    private val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    private val COMMENTS = TokenSet.create(AsmElementTypes.COMMENT)
    private val STRING_LITERALS = TokenSet.create(AsmElementTypes.STRING_LITERAL)

    private val FILE = IFileElementType(AsmLang)

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun createLexer(project: Project?): Lexer = AsmLexerAdapter()

    override fun createParser(project: Project?): PsiParser = AsmParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRING_LITERALS

    override fun createElement(node: ASTNode?): PsiElement = AsmElementTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = AsmFile(viewProvider)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        if (left?.elementType != AsmElementTypes.DOT && right?.elementType == AsmElementTypes.ID)
            return SpaceRequirements.MUST_LINE_BREAK

        return SpaceRequirements.MAY
    }
}