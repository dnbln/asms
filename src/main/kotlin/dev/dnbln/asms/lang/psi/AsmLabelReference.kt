package dev.dnbln.asms.lang.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.descendantsOfType

class AsmLabelReference(element: AsmLabelRef, range: TextRange) : PsiReferenceBase<AsmLabelRef>(element, range),
    PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        val res = multiResolve(false)

        return res.firstOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val labels = element.containingFile.descendantsOfType<AsmLabel>()
            .filter {
                it.name == element.name
            }
            .toList()

        return PsiElementResolveResult.createResults(labels)
    }

    override fun getVariants(): Array<Any> {
        val l = element.containingFile.descendantsOfType<AsmLabel>().map { LookupElementBuilder.create(it) }
            .toList()
        return l.toTypedArray()
    }
}