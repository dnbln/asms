package dev.dnbln.asms.lang.psi

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import dev.dnbln.asms.lang.AsmLexerAdapter


class AsmFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
        AsmLexerAdapter(),
        TokenSet.create(AsmElementTypes.ID),
        TokenSet.create(AsmElementTypes.COMMENT),
        TokenSet.create(
            AsmElementTypes.OCT_LITERAL,
            AsmElementTypes.DEC_LITERAL,
            AsmElementTypes.HEX_LITERAL,
            AsmElementTypes.STRING_LITERAL
        )
    )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is AsmLabel
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return if (element is AsmLabel) {
            "label"
        } else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return if (element is AsmLabel) {
            element.name!!
        } else {
            ""
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return when (element) {
            is AsmLabel -> element.name!! + ":"
            is AsmLabelRef -> element.name!!
            else -> ""
        }
    }
}
