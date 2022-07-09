package dev.dnbln.asms.lang.codeInsight

import com.intellij.openapi.util.KeyWithDefaultValue
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import dev.dnbln.asms.lang.psi.AsmDirective
import dev.dnbln.asms.lang.psi.AsmDirectiveArgName
import dev.dnbln.asms.lang.psi.AsmLabel

class AsmScopeProcessor : PsiScopeProcessor {
    companion object {
        val DEFINED_NAMES_MAP =
            KeyWithDefaultValue.create<MutableMap<String, PsiElement>>("defined.names", mutableMapOf())

        private fun MutableMap<String, PsiElement>.add(k: String, v: PsiElement) {
            this[k] = v
        }
    }

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        val m = state.get(DEFINED_NAMES_MAP)
        when (element) {
            is AsmDirective -> {
                findDirectiveAndCollectArgs(element, object : AsmDirectiveArgsSink {
                    override fun addDeclaration(
                        directive: Directive<out Any>,
                        dir: AsmDirective,
                        nameArg: AsmDirectiveArgName
                    ) {
                        m.add(nameArg.text, nameArg)
                    }

                    override fun addReference(
                        directive: Directive<out Any>,
                        dir: AsmDirective,
                        nameArg: AsmDirectiveArgName
                    ) {}
                })
            }
            is AsmLabel -> {
                m.add(element.possiblyDotId.text, element)
            }
        }

        return true
    }
}