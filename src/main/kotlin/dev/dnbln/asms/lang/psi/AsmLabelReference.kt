package dev.dnbln.asms.lang.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import dev.dnbln.asms.lang.codeInsight.AsmScopeProcessor

class AsmLabelReference(element: AsmLabelRef, range: TextRange) : PsiReferenceBase<AsmLabelRef>(element, range),
    PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        val res = multiResolve(false)

        return res.firstOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val nameLookingFor = element.name!!

        val resolveState = (element.containingFile as AsmFile).resolveDeclarations()

        val m = resolveState.get(AsmScopeProcessor.DEFINED_NAMES_MAP)
        val results = listOfNotNull(m[nameLookingFor])

        return PsiElementResolveResult.createResults(results)
    }

    override fun getVariants(): Array<Any> {
        val resolveState = (element.containingFile as AsmFile).resolveDeclarations()

        val m = resolveState.get(AsmScopeProcessor.DEFINED_NAMES_MAP)

        val results = m.map { l -> LookupElementBuilder.createWithSmartPointer(l.key, l.value) }

        return results.toTypedArray()
    }
}