package dev.dnbln.asms.lang

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import dev.dnbln.asms.lang.codeInsight.REGISTERS
import dev.dnbln.asms.lang.psi.AsmElementTypes
import dev.dnbln.asms.lang.psi.AsmReg

class AsmCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(AsmElementTypes.ID).withParent(AsmReg::class.java),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet,
                ) {
                    resultSet.addAllElements(REGISTERS.map { LookupElementBuilder.create(it.name) })
                }
            })
    }
}