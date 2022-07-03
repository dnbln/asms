package dev.dnbln.asms.lang.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
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
        val results = m[nameLookingFor].orEmpty()

        return PsiElementResolveResult.createResults(results)
    }

    override fun getVariants(): Array<Any> {
        val resolveState = ResolveState()
        AsmScopeProcessor().execute(element.containingFile, resolveState)

        val results =
            resolveState.get(AsmScopeProcessor.DEFINED_NAMES_MAP)
                .flatMap { l -> l.value.map { LookupElementBuilder.createWithSmartPointer(l.key, it) } }

        return results.toTypedArray()
    }
}