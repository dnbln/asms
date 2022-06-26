package dev.dnbln.asms.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.dnbln.asms.lang.codeInsight.findReg
import dev.dnbln.asms.lang.psi.AsmReg
import dev.dnbln.asms.lang.psi.AsmVisitor

class AsmUnknownRegisters : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : AsmVisitor() {
        override fun visitReg(reg: AsmReg) {
            val name = reg.id.text

            if (findReg(name) == null) {
                holder.registerProblem(reg, "Unknown register")
            }
        }
    }
}