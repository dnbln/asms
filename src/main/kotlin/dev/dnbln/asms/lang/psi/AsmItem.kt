package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.siblings

fun Sequence<AsmItem>.removeTrivialNewlines(): Sequence<AsmItem> =
    filter { it.label != null || it.instruction != null || it.directive != null }

val AsmItem.followingItems: Sequence<AsmItem>
    get() = siblings(forward = true, withSelf = false)
        .mapNotNull { it as? AsmItem }
        .removeTrivialNewlines()

val AsmItem.nextItem: AsmItem?
    get() = followingItems.firstOrNull()

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