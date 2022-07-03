package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer

abstract class AsmDirectiveArgNameMixin(node: ASTNode): ASTWrapperPsiElement(node), AsmDirectiveArgName {
    override fun setName(name: String): PsiElement {
        possiblyDotId.replace(createPossiblyDotId(project, name))

        return this
    }

    override fun getNameIdentifier(): PsiElement = possiblyDotId

    override fun getName(): String = nameIdentifier.text

    @Suppress("UnstableApiUsage")
    override fun createPointer(): Pointer<out Symbol> = createSmartPointer()
}