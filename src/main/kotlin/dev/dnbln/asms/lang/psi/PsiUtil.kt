package dev.dnbln.asms.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents

fun PsiElement.rangeIn(parent: PsiElement): TextRange =
    parents(false).takeWhile { it !== parent }.fold(textRangeInParent) { acc, elem ->
        acc.shiftRight(elem.startOffsetInParent)
    }