package dev.dnbln.asms.lang

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import dev.dnbln.asms.lang.psi.AsmDirectiveHead
import dev.dnbln.asms.lang.psi.AsmLabel

class AsmHighlightingAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.parentOfType<AsmLabel>(withSelf = true) != null) {
            val color = AsmColor.LABEL

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(color.textAttributesKey).create()
            return
        }

        if (element.parentOfType<AsmDirectiveHead>(withSelf = true) != null) {
            val color = AsmColor.DIRECTIVE

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(color.textAttributesKey).create()
            return
        }
    }
}
