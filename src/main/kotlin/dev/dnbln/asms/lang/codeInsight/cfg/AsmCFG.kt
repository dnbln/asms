package dev.dnbln.asms.lang.codeInsight.cfg

import dev.dnbln.asms.lang.psi.AsmInstruction
import dev.dnbln.asms.lang.psi.AsmItem
import dev.dnbln.asms.lang.psi.AsmLabel
import dev.dnbln.asms.lang.psi.AsmLabelRef

sealed class AsmCFGNode {
    abstract val instruction: AsmInstruction?
    open val disruptsControlFlowUnconditionally: Boolean = false

    class AsmCFGLabelNode(val label: AsmLabel) : AsmCFGNode() {
        override val instruction: AsmInstruction?
            get() = null
    }

    class AsmCFGUnconditionalJumpNode(override val instruction: AsmInstruction, val labelRef: AsmLabelRef) :
        AsmCFGNode() {
        override val disruptsControlFlowUnconditionally: Boolean = true
    }

    class AsmCFGConditionalJumpNode(override val instruction: AsmInstruction, val labelRef: AsmLabelRef) : AsmCFGNode()
    class AsmCFGCallNode(override val instruction: AsmInstruction, val labelRef: AsmLabelRef) : AsmCFGNode()
    class AsmCFGRetNode(override val instruction: AsmInstruction) : AsmCFGNode() {
        override val disruptsControlFlowUnconditionally: Boolean = true
    }

    class AsmCFGNormalInstrNode(override val instruction: AsmInstruction) : AsmCFGNode()
}

data class AsmCFGNodeId(val id: Int)

sealed class AsmCondition {
    object Equal : AsmCondition()
    object NotEqual : AsmCondition()
    object Zero : AsmCondition()
    object NotZero : AsmCondition()
    object Greater : AsmCondition()
    object NotLessEqual : AsmCondition()
    object GreaterEqual : AsmCondition()
    object NotLess : AsmCondition()
    object Less : AsmCondition()
    object NotGreaterEqual : AsmCondition()
    object LessEqual : AsmCondition()
    object NotGreater : AsmCondition()

    object Above : AsmCondition()
    object NotBelowEqual : AsmCondition()
    object AboveEqual : AsmCondition()
    object NotBelow : AsmCondition()
    object Below : AsmCondition()
    object NotAboveEqual : AsmCondition()
    object BelowEqual : AsmCondition()
    object NotAbove : AsmCondition()

    object CXZero : AsmCondition()
    object Carry : AsmCondition()
    object NoCarry : AsmCondition()
    object Overflow : AsmCondition()
    object NoOverflow : AsmCondition()
    object Parity : AsmCondition()
    object ParityEven : AsmCondition()
    object NoParity : AsmCondition()
    object ParityOdd : AsmCondition()
    object Sign : AsmCondition()
    object NoSign : AsmCondition()
}

sealed class AsmCFGArcKind {
    object Fallthrough : AsmCFGArcKind()
    object Call : AsmCFGArcKind()
    object UnconditionalJmp : AsmCFGArcKind()
    class ConditionalJmp(val condition: AsmCondition) : AsmCFGArcKind()
}

data class AsmCFGArc(val from: AsmCFGNodeId, val to: AsmCFGNodeId, val kind: AsmCFGArcKind)

data class AsmCFGArcId(val id: Int)

class AsmCFG {
    val nodes = mutableListOf<AsmCFGNode>()
    val arcs = mutableListOf<AsmCFGArc>()

    fun incoming(nodeId: AsmCFGNodeId): Sequence<AsmCFGArc> = arcs.asSequence().filter { it.to == nodeId }
    fun incoming(node: AsmCFGNode) = incoming(idOfNode(node))

    fun incomingIds(nodeId: AsmCFGNodeId) = arcs.asSequence().withIndex().filter { it.value.to == nodeId }.map { AsmCFGArcId(it.index) }
    fun incomingIds(node: AsmCFGNode) = incomingIds(idOfNode(node))

    fun outgoing(nodeId: AsmCFGNodeId): Sequence<AsmCFGArc> = arcs.asSequence().filter { it.from == nodeId }
    fun outgoing(node: AsmCFGNode) = outgoing(idOfNode(node))

    fun outgoingIds(nodeId: AsmCFGNodeId) = arcs.asSequence().withIndex().filter { it.value.from == nodeId }.map { AsmCFGArcId(it.index) }
    fun outgoingIds(node: AsmCFGNode) = outgoingIds(idOfNode(node))

    fun idOfNode(node: AsmCFGNode) = AsmCFGNodeId(nodes.indexOf(node))
    fun idOfArc(arc: AsmCFGArc) = AsmCFGArcId(arcs.indexOf(arc))

    fun nodeOfInstruction(instruction: AsmInstruction): AsmCFGNodeId? =
        nodes.withIndex().find { it.value.instruction === instruction }?.index?.let(::AsmCFGNodeId)

    fun nodeOfLabel(label: AsmLabel): AsmCFGNodeId? =
        nodes.withIndex().find {
            when (val labelNode = it.value) {
                is AsmCFGNode.AsmCFGLabelNode -> labelNode.label === label
                else -> false
            }
        }?.index?.let(::AsmCFGNodeId)

    fun nodeOfItem(item: AsmItem): AsmCFGNodeId? {
        val instruction = item.instruction
        if (instruction != null) return nodeOfInstruction(instruction)

        val label = item.label
        if (label != null) return nodeOfLabel(label)

        return null
    }

    fun add(node: AsmCFGNode): AsmCFGNodeId {
        nodes.add(node)

        return AsmCFGNodeId(nodes.size - 1)
    }

    fun addArc(arc: AsmCFGArc): AsmCFGArcId {
        arcs.add(arc)

        return AsmCFGArcId(arcs.size - 1)
    }

    fun findFallthrough(nodeId: AsmCFGNodeId): AsmCFGArcId? = outgoingIds(nodeId).find { this[it].kind == AsmCFGArcKind.Fallthrough }

    fun lookupLabel(label: AsmLabelRef): AsmCFGNodeId? =
        nodes.indexOfFirst { (it as? AsmCFGNode.AsmCFGLabelNode)?.label?.name == label.name }.takeIf { it != -1 }?.let(::AsmCFGNodeId)

    operator fun get(id: AsmCFGNodeId): AsmCFGNode = nodes[id.id]
    operator fun get(id: AsmCFGArcId): AsmCFGArc = arcs[id.id]
}