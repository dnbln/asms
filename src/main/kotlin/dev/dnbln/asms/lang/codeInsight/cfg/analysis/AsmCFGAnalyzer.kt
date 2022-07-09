package dev.dnbln.asms.lang.codeInsight.cfg.analysis

import dev.dnbln.asms.lang.codeInsight.cfg.AsmCFG
import dev.dnbln.asms.lang.codeInsight.cfg.AsmCFGNode
import dev.dnbln.asms.lang.codeInsight.cfg.AsmCFGNodeId
import dev.dnbln.asms.lang.codeInsight.findInstruction
import dev.dnbln.asms.lang.psi.*
import java.util.*
import kotlin.collections.ArrayDeque

sealed class AsmCFGAnalysisDiagnostic {
    class DeadCode(val instruction: AsmInstruction) : AsmCFGAnalysisDiagnostic()
    class DeadLabel(val label: AsmLabel) : AsmCFGAnalysisDiagnostic()
}

class AsmCFGAnalysis {
    val diagnostics = mutableListOf<AsmCFGAnalysisDiagnostic>()
}

internal sealed class CfqElement {
    class Label(val label: AsmLabel, val node: AsmCFGNodeId) : CfqElement()
    class Instruction(val instruction: AsmInstruction, val node: AsmCFGNodeId): CfqElement()
}

class AsmCFGAnalysisInternal {
    val codeLiveness: IdentityHashMap<AsmInstruction, Boolean> = IdentityHashMap()
    val labelLiveness: IdentityHashMap<AsmLabel, Boolean> = IdentityHashMap()
    internal val codeFlowQueue: ArrayDeque<CfqElement> = ArrayDeque()

    fun addNodeToCFQ(node: AsmCFGNode, nodeId: AsmCFGNodeId) {
        when (node) {
            is AsmCFGNode.AsmCFGLabelNode -> {
                if (labelLiveness[node.label] == true) return
                labelLiveness[node.label] = true

                codeFlowQueue.add(CfqElement.Label(node.label, nodeId))
            }
            is AsmCFGNode.AsmCFGCallNode,
            is AsmCFGNode.AsmCFGConditionalJumpNode,
            is AsmCFGNode.AsmCFGNormalInstrNode,
            is AsmCFGNode.AsmCFGRetNode,
            is AsmCFGNode.AsmCFGUnconditionalJumpNode -> {
                val instruction = node.instruction!!

                if (codeLiveness[instruction] == true) return
                codeLiveness[instruction] = true

                codeFlowQueue.add(CfqElement.Instruction(instruction, nodeId))
            }
        }
    }
}

class AsmCFGAnalyzer(val file: AsmFile, val cfg: AsmCFG) {
    fun analyze(): AsmCFGAnalysis {
        val anl = AsmCFGAnalysis()

        val anlInternal = AsmCFGAnalysisInternal()
        for (item in file.items) {
            val instruction = item.instruction ?: continue
            anlInternal.codeLiveness[instruction] = false
        }

        for (item in file.items) {
            val label = item.label ?: continue
            val ext = label.externallyVisible
            anlInternal.labelLiveness[label] = ext

            if (!ext) continue

            anlInternal.codeFlowQueue.add(CfqElement.Label(label, cfg.nodeOfLabel(label)!!))
        }

        while (anlInternal.codeFlowQueue.isNotEmpty()) {
            when (val first = anlInternal.codeFlowQueue.removeFirst()) {
                is CfqElement.Label -> {
                    for (outgoing in cfg.outgoing(first.node)) {
                        anlInternal.addNodeToCFQ(cfg[outgoing.to], outgoing.to)
                    }
                }
                is CfqElement.Instruction -> {
                    for (outgoing in cfg.outgoing(first.node)) {
                        anlInternal.addNodeToCFQ(cfg[outgoing.to], outgoing.to)
                    }
                }
            }
        }

        for (item in file.items) {
            val label = item.label ?: continue

            if (anlInternal.labelLiveness[label] != true)
                anl.diagnostics.add(AsmCFGAnalysisDiagnostic.DeadLabel(label))
        }

        for (item in file.items) {
            val instruction = item.instruction ?: continue

            if (anlInternal.codeLiveness[instruction] != true)
                anl.diagnostics.add(AsmCFGAnalysisDiagnostic.DeadCode(instruction))
        }

        return anl
    }
}