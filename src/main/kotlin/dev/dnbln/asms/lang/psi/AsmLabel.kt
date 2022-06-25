package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class AsmLabelMixin(node: ASTNode): ASTWrapperPsiElement(node),
    AsmLabel {
    override fun setName(name: String): PsiElement {
        possiblyDotId.replace(createPossiblyDotId(project, name))

        return this
    }

    override fun getNameIdentifier(): PsiElement = possiblyDotId
    override fun getName(): String = nameIdentifier.text
}