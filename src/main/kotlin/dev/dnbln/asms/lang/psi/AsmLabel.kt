package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.refactoring.suggested.createSmartPointer
import dev.dnbln.asms.lang.codeInsight.AsmDirectiveArgsSink
import dev.dnbln.asms.lang.codeInsight.Directive
import dev.dnbln.asms.lang.codeInsight.Global
import dev.dnbln.asms.lang.codeInsight.findDirectiveAndCollectArgs

val AsmLabel.externallyVisible: Boolean
    get() {
        val file = containingFile as AsmFile

        val directives = file.items.mapNotNull { it.directive }
        val label = this

        return directives.any {
            var isGlobal = false

            findDirectiveAndCollectArgs(it, object : AsmDirectiveArgsSink {
                override fun addDeclaration(
                    directive: Directive<out Any>,
                    dir: AsmDirective,
                    nameArg: AsmDirectiveArgName
                ) {
                }

                override fun addReference(
                    directive: Directive<out Any>,
                    dir: AsmDirective,
                    nameArg: AsmDirectiveArgName
                ) {
                    if (directive !== Global) return

                    if (nameArg.name != label.name) return

                    isGlobal = true
                }
            })

            isGlobal
        }
    }

val AsmLabel.itemsAfterLabel: Sequence<AsmItem>
    get() {
        val file = containingFile as AsmFile
        val itemSequence = file.items.asSequence()

        return itemSequence.dropWhile { it.label !== this }.drop(1)
    }

abstract class AsmLabelMixin(node: ASTNode) : ASTWrapperPsiElement(node),
    AsmLabel {
    override fun setName(name: String): PsiElement {
        possiblyDotId.replace(createPossiblyDotId(project, name))

        return this
    }

    override fun getNameIdentifier(): PsiElement = possiblyDotId
    override fun getName(): String = nameIdentifier.text

    @Suppress("UnstableApiUsage")
    override fun createPointer(): Pointer<out Symbol> = createSmartPointer()

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean = processor.execute(this, state)
}