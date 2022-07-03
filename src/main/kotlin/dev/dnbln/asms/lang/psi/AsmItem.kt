package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

abstract class AsmItemMixin(node: ASTNode) : ASTWrapperPsiElement(node), AsmItem {
    override fun processDeclarations(
        processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement
    ): Boolean {
        for (child in children) {
            if (child === lastParent) continue

            if (!child.processDeclarations(processor, state, lastParent, place)) return false
        }

        return true
    }
}