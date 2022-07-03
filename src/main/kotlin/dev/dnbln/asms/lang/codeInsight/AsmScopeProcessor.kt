package dev.dnbln.asms.lang.codeInsight

import com.intellij.openapi.util.KeyWithDefaultValue
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import dev.dnbln.asms.lang.psi.AsmDirective
import dev.dnbln.asms.lang.psi.AsmDirectiveArgName
import dev.dnbln.asms.lang.psi.AsmLabel
import dev.dnbln.asms.lang.psi.AsmVisitor

class AsmScopeProcessor : PsiScopeProcessor {
    companion object {
        val DEFINED_NAMES_MAP =
            KeyWithDefaultValue.create<MutableMap<String, MutableList<PsiElement>>>("defined.names", mutableMapOf())

        private fun MutableMap<String, MutableList<PsiElement>>.add(k: String, v: PsiElement) {
            if (containsKey(k))
                this[k]!!.add(v)
            else
                this[k] = mutableListOf(v)
        }
    }

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        val m = state.get(DEFINED_NAMES_MAP)
        when (element) {
            is AsmDirective -> {
                findDirectiveAndCollectArgs(element, object : AsmDirectiveArgsSink {
                    override fun addDeclaration(dir: AsmDirective, nameArg: AsmDirectiveArgName) {
                        m.add(nameArg.text, nameArg)
                    }

                    override fun addReference(dir: AsmDirective, nameArg: AsmDirectiveArgName) {}
                })
            }
            is AsmLabel -> {
                m.add(element.possiblyDotId.text, element)
            }
        }

        return true
    }
}