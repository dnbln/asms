package dev.dnbln.asms.lang.psi

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import dev.dnbln.asms.lang.codeInsight.AsmDirectiveArgsCollector
import dev.dnbln.asms.lang.codeInsight.AsmDirectiveArgsSink
import dev.dnbln.asms.lang.codeInsight.findDirective
import dev.dnbln.asms.lang.codeInsight.findDirectiveAndCollectArgs

@Suppress("UnstableApiUsage")
class AsmDeclarationProvider : PsiSymbolDeclarationProvider {
    override fun getDeclarations(
        element: PsiElement,
        offsetInElement: Int
    ): MutableCollection<out PsiSymbolDeclaration> {
        val l = mutableListOf<PsiSymbolDeclaration>()

        when (element) {
            is AsmDirective -> findDirectiveAndCollectArgs(element, AsmDirectiveArgsCollector(l))
            is AsmLabel -> {
                l.add(object: PsiSymbolDeclaration {
                    override fun getDeclaringElement(): PsiElement = element

                    override fun getRangeInDeclaringElement(): TextRange = element.possiblyDotId.textRangeInParent

                    override fun getSymbol(): Symbol = element
                })
            }
        }

        return l
    }
}