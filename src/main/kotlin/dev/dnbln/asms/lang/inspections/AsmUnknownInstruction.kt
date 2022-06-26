package dev.dnbln.asms.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.dnbln.asms.lang.codeInsight.findInstruction
import dev.dnbln.asms.lang.psi.AsmInstruction
import dev.dnbln.asms.lang.psi.AsmVisitor

class AsmUnknownInstruction : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : AsmVisitor() {
            override fun visitInstruction(instr: AsmInstruction) {
                val ins = findInstruction(instr)

                if (ins == null) {
                    holder.registerProblem(instr, "Unknown instruction (may be due to mismatch of operand sizes)")
                }
            }
        }
}