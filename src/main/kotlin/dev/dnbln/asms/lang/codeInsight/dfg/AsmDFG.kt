// Further reading: https://en.wikipedia.org/wiki/Data-flow_analysis

package dev.dnbln.asms.lang.codeInsight.dfg

import dev.dnbln.asms.lang.psi.AsmInstruction
import dev.dnbln.asms.lang.psi.AsmMem
import dev.dnbln.asms.lang.psi.AsmReg

sealed class AsmDFGNode {
    class AsmDFGNodeRegister(register: AsmReg) : AsmDFGNode()
    class AsmDFGNodeImplicitRegister(instruction: AsmInstruction): AsmDFGNode()
    class AsmDFGNodeMemory(memory: AsmMem): AsmDFGNode()
    class AsmDFGNodeWeakMemory(memory: AsmMem): AsmDFGNode() // lea instruction kind of memory access
}

data class AsmDFGNodeId(internal val id: Int)

class AsmDFGArc(val from: AsmDFGNodeId, val to: AsmDFGNodeId)

data class AsmDFGArcId(internal val id: Int)

class AsmDFG {
    val nodes = mutableListOf<AsmDFGNode>()
    val arcs = mutableListOf<AsmDFGArc>()

    operator fun get(nodeId: AsmDFGNodeId) = nodes[nodeId.id]
    operator fun get(arcId: AsmDFGArcId) = arcs[arcId.id]
}