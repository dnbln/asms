package dev.dnbln.asms.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.dnbln.asms.lang.psi.AsmLabelRef
import dev.dnbln.asms.lang.psi.AsmVisitor

class AsmUnknownNameRef : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : AsmVisitor() {
            override fun visitLabelRef(ref: AsmLabelRef) {
                val reference = ref.reference
                if (reference != null && reference.resolve() == null) {
                    holder.registerProblem(reference)
                }
            }
        }
}