package dev.dnbln.asms.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.dnbln.asms.lang.codeInsight.OpKind
import dev.dnbln.asms.lang.codeInsight.findInstruction
import dev.dnbln.asms.lang.psi.AsmInstruction
import dev.dnbln.asms.lang.psi.AsmInstructionArg
import dev.dnbln.asms.lang.psi.AsmMem
import dev.dnbln.asms.lang.psi.AsmVisitor

private fun AsmMem.isSafe(): Boolean {
    if (this.memSegmentOverride != null || this.memBaseIndexScale != null) return true

    val list = memOffset?.memOffsetElementList ?: return true

    if (list.all { it.num != null }) return false

    return true
}

class AsmLikelySegfault : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        object : AsmVisitor() {
            fun verifyIsSafe(opkind: OpKind, arg: AsmInstructionArg): Boolean {
                if (opkind != OpKind.Mem) return true
                val mem = arg.mem ?: return true
                return mem.isSafe()
            }

            override fun visitInstruction(ins: AsmInstruction) {
                val (instr, variant) = findInstruction(ins) ?: return
                if (instr.memSafeAlways) return
                if (variant == null) return

                val argList = ins.instructionArgList.instructionArgList
                if (!verifyIsSafe(variant.op1 ?: return, argList[0]))
                    holder.registerProblem(argList[0], "Likely to segfault")
                if (!verifyIsSafe(variant.op2 ?: return, argList[1]))
                    holder.registerProblem(argList[0], "Likely to segfault")
                if (!verifyIsSafe(variant.op3 ?: return, argList[2]))
                    holder.registerProblem(argList[0], "Likely to segfault")
            }
        }
}