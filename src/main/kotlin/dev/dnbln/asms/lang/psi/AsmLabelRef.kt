package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

abstract class AsmLabelRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), AsmLabelRef {
    override fun getReference(): PsiReference? = AsmLabelReference(this, TextRange.allOf(this.text))

    override fun setName(name: String): PsiElement {
        val labelRef = createLabelRef(project, name)
        replace(labelRef)

        return labelRef
    }

    override fun getNameIdentifier(): PsiElement = possiblyDotId
    override fun getName(): String? = nameIdentifier.text
}