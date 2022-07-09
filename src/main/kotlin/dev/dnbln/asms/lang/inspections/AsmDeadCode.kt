package dev.dnbln.asms.lang.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import dev.dnbln.asms.lang.codeInsight.cfg.analysis.AsmCFGAnalysisDiagnostic
import dev.dnbln.asms.lang.codeInsight.cfg.analysis.AsmCFGAnalyzer
import dev.dnbln.asms.lang.codeInsight.cfg.buildCFG
import dev.dnbln.asms.lang.psi.AsmFile

class AsmDeadCode : LocalInspectionTool() {
    override fun runForWholeFile(): Boolean = true

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is AsmFile) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)

        val cfg = buildCFG(file)

        val analysis = AsmCFGAnalyzer(file, cfg).analyze()

        for (diagnostic in analysis.diagnostics) {
            when (diagnostic) {
                is AsmCFGAnalysisDiagnostic.DeadCode -> holder.registerProblem(diagnostic.instruction, "Dead code")
                is AsmCFGAnalysisDiagnostic.DeadLabel -> holder.registerProblem(diagnostic.label, "Dead label")
            }
        }

        return holder.resultsArray
    }
}