package dev.dnbln.asms.lang.codeInsight.cfg

import dev.dnbln.asms.lang.codeInsight.AsmScopeProcessor
import dev.dnbln.asms.lang.codeInsight.findInstruction
import dev.dnbln.asms.lang.psi.AsmFile
import dev.dnbln.asms.lang.psi.nextItem

fun buildCFG(asmFile: AsmFile): AsmCFG {
    val resolveState = asmFile.resolveDeclarations()
    val names = resolveState.get(AsmScopeProcessor.DEFINED_NAMES_MAP)

    val cfg = AsmCFG()

    for (item in asmFile.items) {
        val label = item.label ?: continue

        cfg.add(AsmCFGNode.AsmCFGLabelNode(label))
    }

    for (item in asmFile.items) {
        val instruction = item.instruction ?: continue

        val (instr, variant) = findInstruction(instruction) ?: continue

        if (instr.buildCFG != null) {
            (instr.buildCFG)(instruction, variant, cfg, names)
        } else {
            cfg.add(AsmCFGNode.AsmCFGNormalInstrNode(instruction))
        }
    }

    for (item in asmFile.items) {
        val nextItem = item.nextItem

        if (nextItem != null) {
            val inode = cfg.nodeOfItem(item) ?: continue
            val onode = cfg.nodeOfItem(nextItem) ?: continue

            if (cfg[inode].disruptsControlFlowUnconditionally) continue

            cfg.addArc(AsmCFGArc(inode, onode, AsmCFGArcKind.Fallthrough))
        }
    }

    return cfg
}