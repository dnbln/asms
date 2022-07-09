package dev.dnbln.asms.lang.codeInsight

import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import dev.dnbln.asms.lang.codeInsight.OpKind.*
import dev.dnbln.asms.lang.codeInsight.RegisterKind.*
import dev.dnbln.asms.lang.codeInsight.cfg.*
import dev.dnbln.asms.lang.codeInsight.inlay.AsmInlayPresentationFactory
import dev.dnbln.asms.lang.psi.AsmInstruction
import dev.dnbln.asms.lang.psi.label

sealed class OpKind {
    class Reg(val kind: RegisterKind) : OpKind() {
        override fun toString(): String = "REG($kind)"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Reg

            if (kind != other.kind) return false

            return true
        }

        override fun hashCode(): Int {
            return kind.hashCode()
        }
    }

    object Imm : OpKind() {
        override fun toString(): String = "IMM"
    }

    object Mem : OpKind() {
        override fun toString(): String = "MEM"
    }

    object Indirection : OpKind() {
        override fun toString(): String = "*INDIRECTION"
    }
}

data class Suffix(val suffix: String, val mandatory: Boolean = false)

data class InstructionVariant(
    val op1: OpKind? = null,
    val op2: OpKind? = null,
    val op3: OpKind? = null,
    val suffix: Suffix? = null,
    val data: Any? = null, // any extra data that would help differentiate variants
) {
    fun matchedBy(ops: List<OpKind>, suffix: String): Boolean {
        if (suffix != "" && suffix != this.suffix?.suffix) return false

        if (this.suffix?.mandatory == true && suffix == "") return false

        if (ops.size != listOfNotNull(op1, op2, op3).size) return false

        if (op1 != null && op1 != ops[0]) return false
        if (op2 != null && op2 != ops[1]) return false
        if (op3 != null && op3 != ops[2]) return false

        return true
    }
}

data class InstructionVariants(val list: Array<out InstructionVariant>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InstructionVariants

        if (!list.contentEquals(other.list)) return false

        return true
    }

    override fun hashCode(): Int {
        return list.contentHashCode()
    }
}

fun variants(vararg list: InstructionVariant): InstructionVariants = InstructionVariants(list)

@Suppress("UnstableApiUsage")
data class Instruction(
    val mnemonic: String,
    val variants: InstructionVariants? = null,
    val memSafeAlways: Boolean = false,
    val present: ((instruction: AsmInstruction, variant: InstructionVariant?, factory: AsmInlayPresentationFactory) -> InlayPresentation?)? = null,
    val buildCFG: ((instruction: AsmInstruction, variant: InstructionVariant?, cfg: AsmCFG, names: Map<String, PsiElement>) -> Unit)? = null,
)


val ARITHMETIC_INSTRUCTION_VARIANTS = variants(
    // 8 bit
    InstructionVariant(
        Imm, Reg(Reg8), suffix = Suffix("b")
    ),
    InstructionVariant(
        Reg(Reg8), Reg(Reg8), suffix = Suffix("b")
    ),
    InstructionVariant(
        Imm, Mem, suffix = Suffix("b", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg8), Mem, suffix = Suffix("b")
    ),
    InstructionVariant(
        Mem, Reg(Reg8), suffix = Suffix("b")
    ),

    // 16 bit
    InstructionVariant(
        Imm, Reg(Reg16), suffix = Suffix("w")
    ),
    InstructionVariant(
        Reg(Reg16), Reg(Reg16), suffix = Suffix("w")
    ),
    InstructionVariant(
        Imm, Mem, suffix = Suffix("w", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg16), Mem, suffix = Suffix("w")
    ),
    InstructionVariant(
        Mem, Reg(Reg16), suffix = Suffix("w")
    ),

    // 32 bit
    InstructionVariant(
        Imm, Reg(Reg32), suffix = Suffix("l")
    ),
    InstructionVariant(
        Reg(Reg32), Reg(Reg32), suffix = Suffix("l")
    ),
    InstructionVariant(
        Imm, Mem, suffix = Suffix("l", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg32), Mem, suffix = Suffix("l")
    ),
    InstructionVariant(
        Mem, Reg(Reg32), suffix = Suffix("l")
    ),

    // 64 bit
    InstructionVariant(
        Imm, Reg(Reg64), suffix = Suffix("q")
    ),
    InstructionVariant(
        Reg(Reg64), Reg(Reg64), suffix = Suffix("q")
    ),
    InstructionVariant(
        Imm, Mem, suffix = Suffix("q", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg64), Mem, suffix = Suffix("q")
    ),
    InstructionVariant(
        Mem, Reg(Reg64), suffix = Suffix("q")
    ),
)

val ADD = Instruction("add", ARITHMETIC_INSTRUCTION_VARIANTS, present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" += "), factory.presentInstructionArg(left)
    )
})
val AND = Instruction("and", ARITHMETIC_INSTRUCTION_VARIANTS, present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" &= "), factory.presentInstructionArg(left)
    )
})

val CALL = Instruction("call", variants(
    InstructionVariant(
        Mem, suffix = Suffix("q")
    )
), present = { instruction, _, factory ->
    val (call) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(call),
        factory.text(" ()"),
    )
}, buildCFG = { instruction, _, cfg, _ ->
    val label = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val callInstr = cfg.add(AsmCFGNode.AsmCFGCallNode(instruction, label))

    if (label.reference?.resolve() == null) return@Instruction

    val target = cfg.lookupLabel(label) ?: return@Instruction

    cfg.addArc(AsmCFGArc(callInstr, target, AsmCFGArcKind.Call))
})

val CMP = Instruction("cmp", variants(
    // 8 bit
    InstructionVariant(Imm, Reg(Reg8), suffix = Suffix("b")),
    InstructionVariant(Imm, Mem, suffix = Suffix("b", mandatory = true)),
    InstructionVariant(Reg(Reg8), Reg(Reg8), suffix = Suffix("b")),
    InstructionVariant(Reg(Reg8), Mem, suffix = Suffix("b")),
    InstructionVariant(Mem, Reg(Reg8), suffix = Suffix("b")),

    // 16 bit
    InstructionVariant(Imm, Reg(Reg16), suffix = Suffix("w")),
    InstructionVariant(Imm, Mem, suffix = Suffix("w", mandatory = true)),
    InstructionVariant(Reg(Reg16), Reg(Reg16), suffix = Suffix("w")),
    InstructionVariant(Reg(Reg16), Mem, suffix = Suffix("w")),
    InstructionVariant(Mem, Reg(Reg16), suffix = Suffix("w")),

    // 32 bit
    InstructionVariant(Imm, Reg(Reg32), suffix = Suffix("l")),
    InstructionVariant(Imm, Mem, suffix = Suffix("l", mandatory = true)),
    InstructionVariant(Reg(Reg32), Reg(Reg32), suffix = Suffix("l")),
    InstructionVariant(Reg(Reg32), Mem, suffix = Suffix("l")),
    InstructionVariant(Mem, Reg(Reg32), suffix = Suffix("l")),

    // 64 bit
    InstructionVariant(Imm, Reg(Reg64), suffix = Suffix("q")),
    InstructionVariant(Imm, Mem, suffix = Suffix("q", mandatory = true)),
    InstructionVariant(Reg(Reg64), Reg(Reg64), suffix = Suffix("q")),
    InstructionVariant(Reg(Reg64), Mem, suffix = Suffix("q")),
    InstructionVariant(Mem, Reg(Reg64), suffix = Suffix("q")),
), present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" cmp "), factory.presentInstructionArg(left)
    )
})

val JMP_VARIANTS = variants(
    InstructionVariant(Mem, suffix = Suffix("q")),
    InstructionVariant(Indirection, suffix = Suffix("q")),
    InstructionVariant(Mem, suffix = Suffix("l")),
    InstructionVariant(Indirection, suffix = Suffix("l")),
)

val JA = Instruction("ja", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text(">")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.Above)))
})
val JAE = Instruction("jae", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text(">=")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.AboveEqual)))
})
val JB = Instruction("jb", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("<")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.Below)))
})
val JBE = Instruction("jbe", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("<=")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.BelowEqual)))
})
val JE = Instruction("je", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("==")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.Equal)))
})
val JG = Instruction("jg", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text(">")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.Greater)))
})
val JGE = Instruction("jge", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text(">=")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.GreaterEqual)))
})
val JL = Instruction("jl", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("<")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.Less)))
})
val JLE = Instruction("jle", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("<=")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.LessEqual)))
})
val JMP = Instruction("jmp", JMP_VARIANTS, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGUnconditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.UnconditionalJmp))
})
val JNA = Instruction("jna", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("<= [not >]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotAbove)))
})
val JNAE = Instruction("jnae", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("< [not >=]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotAboveEqual)))
})
val JNB = Instruction("jnb", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text(">= [not <]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotBelow)))
})
val JNBE = Instruction("jnbe", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("> [not <=]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotBelowEqual)))
})
val JNE = Instruction("jne", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("!=")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotEqual)))
})
val JNG = Instruction("jng", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("<= [not >]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotGreater)))
})
val JNGE = Instruction("jnge", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("< [not >=]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotGreaterEqual)))
})
val JNL = Instruction("jnl", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text(">= [not <]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotLess)))
})
val JNLE = Instruction("jnle", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("> [not <=]")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotLessEqual)))
})
val JNZ = Instruction("jnz", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("!= 0")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.NotZero)))
})
val JZ = Instruction("jz", JMP_VARIANTS, present = { _, _, factory ->
    @Suppress("UnstableApiUsage") factory.text("== 0")
}, buildCFG = { instruction, _, cfg, _ ->
    val labelRef = instruction.instructionArgList.instructionArgList[0].mem?.label ?: return@Instruction

    val jmpInstr = cfg.add(AsmCFGNode.AsmCFGConditionalJumpNode(instruction, labelRef))
    val targetLabel = cfg.lookupLabel(labelRef) ?: return@Instruction

    cfg.addArc(AsmCFGArc(jmpInstr, targetLabel, AsmCFGArcKind.ConditionalJmp(AsmCondition.Zero)))
})

val LEA = Instruction("lea", variants(
    InstructionVariant(
        Mem, Reg(Reg32), suffix = Suffix("l")
    ),
    InstructionVariant(
        Mem, Reg(Reg64), suffix = Suffix("q")
    ),
), memSafeAlways = true, present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" = &"), factory.presentInstructionArg(left)
    )
})

val MOV = Instruction("mov", variants(
    // 8 bit move
    InstructionVariant(
        Imm, Reg(Reg8), suffix = Suffix("b")
    ), InstructionVariant(
        Imm, Mem, suffix = Suffix("b", mandatory = true)
    ), InstructionVariant(
        Reg(Reg8), Reg(Reg8), suffix = Suffix("b")
    ), InstructionVariant(
        Reg(Reg8), Mem, suffix = Suffix("b")
    ), InstructionVariant(
        Mem, Reg(Reg8), suffix = Suffix("b")
    ),

    // 16 bit move
    InstructionVariant(
        Imm, Reg(Reg16), suffix = Suffix("w")
    ), InstructionVariant(
        Imm, Mem, suffix = Suffix("w", mandatory = true)
    ), InstructionVariant(
        Reg(Reg16), Reg(Reg16), suffix = Suffix("w")
    ), InstructionVariant(
        Reg(Reg16), Mem, suffix = Suffix("w")
    ), InstructionVariant(
        Mem, Reg(Reg16), suffix = Suffix("w")
    ),

    // 32 bit move
    InstructionVariant(
        Imm, Reg(Reg32), suffix = Suffix("l")
    ), InstructionVariant(
        Imm, Mem, suffix = Suffix("l", mandatory = true)
    ), InstructionVariant(
        Reg(Reg32), Reg(Reg32), suffix = Suffix("l")
    ), InstructionVariant(
        Reg(Reg32), Mem, suffix = Suffix("l")
    ), InstructionVariant(
        Mem, Reg(Reg32), suffix = Suffix("l")
    ),

    // 64 bit move
    InstructionVariant(
        Imm, Reg(Reg64), suffix = Suffix("q")
    ), InstructionVariant(
        Imm, Mem, suffix = Suffix("q", mandatory = true)
    ), InstructionVariant(
        Reg(Reg64), Reg(Reg64), suffix = Suffix("q")
    ), InstructionVariant(
        Reg(Reg64), Mem, suffix = Suffix("q")
    ), InstructionVariant(
        Mem, Reg(Reg64), suffix = Suffix("q")
    )
), present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" = "), factory.presentInstructionArg(left)
    )
})

val MOVZX = Instruction("movzx", variants(
    InstructionVariant(
        Reg(Reg8), Reg(Reg16), suffix = Suffix("b")
    ), InstructionVariant(
        Mem, Reg(Reg16), suffix = Suffix("b", mandatory = true)
    ), InstructionVariant(
        Reg(Reg8), Reg(Reg32), suffix = Suffix("b")
    ), InstructionVariant(
        Mem, Reg(Reg32), suffix = Suffix("b")
    ), InstructionVariant(
        Reg(Reg8), Reg(Reg64), suffix = Suffix("b")
    ), InstructionVariant(
        Mem, Reg(Reg64), suffix = Suffix("b", mandatory = true)
    ), InstructionVariant(
        Reg(Reg16), Reg(Reg32), suffix = Suffix("w")
    ), InstructionVariant(
        Mem, Reg(Reg32), suffix = Suffix("w", mandatory = true)
    ), InstructionVariant(
        Reg(Reg16), Reg(Reg64), suffix = Suffix("w")
    ), InstructionVariant(
        Mem, Reg(Reg64), suffix = Suffix("w", mandatory = true)
    )
), present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right),
        factory.text(" = ZX("),
        factory.presentInstructionArg(left),
        factory.text(")")
    )
})

val MUL = Instruction(
    "mul",
    variants(
        // 8 bit
        InstructionVariant(
            Reg(Reg8),
            suffix = Suffix("b"),
            data = 8,
        ),
        InstructionVariant(
            Mem,
            suffix = Suffix("b", mandatory = true),
            data = 8,
        ),

        // 16 bit
        InstructionVariant(
            Reg(Reg16),
            suffix = Suffix("w"),
            data = 16,
        ),
        InstructionVariant(
            Mem,
            suffix = Suffix("w", mandatory = true),
            data = 16,
        ),

        // 32 bit
        InstructionVariant(
            Reg(Reg32),
            suffix = Suffix("l"),
            data = 32,
        ),
        InstructionVariant(
            Mem,
            suffix = Suffix("l", mandatory = true),
            data = 32,
        ),

        // 64 bit
        InstructionVariant(
            Reg(Reg64),
            suffix = Suffix("q"),
            data = 64,
        ),
        InstructionVariant(
            Mem,
            suffix = Suffix("q", mandatory = true),
            data = 64,
        ),
    ),
    present = { instruction, variant, factory ->

        val (arg) = instruction.instructionArgList.instructionArgList

        val bits = variant!!.data as Int

        @Suppress("UnstableApiUsage") when (bits) {
            8 -> factory.factory.seq(
                factory.text("%ax = %al * "),
                factory.presentInstructionArg(arg),
            )
            16 -> factory.factory.seq(
                factory.text("%dx:%ax = %ax * "),
                factory.presentInstructionArg(arg),
            )
            32 -> factory.factory.seq(
                factory.text("%edx:%eax = %eax * "),
                factory.presentInstructionArg(arg),
            )
            64 -> factory.factory.seq(
                factory.text("%rdx:%rax = %rax * "),
                factory.presentInstructionArg(arg),
            )
            else -> error("Unknown size for mul")
        }
    },
)

val POP = Instruction(
    "pop", variants(
        InstructionVariant(suffix = Suffix("q")), InstructionVariant(
            Reg(Reg64), suffix = Suffix("q")
        ), InstructionVariant(
            Mem, suffix = Suffix("q", mandatory = true)
        )
    )
)
val POPF = Instruction(
    "popf", variants(
        InstructionVariant(suffix = Suffix("q"))
    )
)
val PUSH = Instruction(
    "push",
    variants(
        InstructionVariant(
            Imm, suffix = Suffix("q")
        ), InstructionVariant(
            Reg(Reg64), suffix = Suffix("q")
        ), InstructionVariant(
            Mem, suffix = Suffix("q", mandatory = true)
        )
    ),
)
val PUSHF = Instruction(
    "pushf", variants(
        InstructionVariant(suffix = Suffix("q"))
    )
)

val RET = Instruction(
    "ret", variants(InstructionVariant(suffix = Suffix("q")), InstructionVariant(suffix = Suffix("l"))),
    buildCFG = { instruction, _, cfg, _ ->
        cfg.add(AsmCFGNode.AsmCFGRetNode(instruction))
    }
)

val OR = Instruction("or", ARITHMETIC_INSTRUCTION_VARIANTS, present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" |= "), factory.presentInstructionArg(left)
    )
})
val SUB = Instruction("sub", ARITHMETIC_INSTRUCTION_VARIANTS, present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" -= "), factory.presentInstructionArg(left)
    )
})
val SYSCALL = Instruction("syscall")
val XOR = Instruction("xor", ARITHMETIC_INSTRUCTION_VARIANTS, present = { instruction, _, factory ->
    val (left, right) = instruction.instructionArgList.instructionArgList

    @Suppress("UnstableApiUsage") factory.factory.seq(
        factory.presentInstructionArg(right), factory.text(" ^= "), factory.presentInstructionArg(left)
    )
})

val INSTRUCTIONS = setOf(
    ADD,
    AND,
    CALL,
    CMP,
    JA,
    JAE,
    JB,
    JBE,
    JE,
    JG,
    JGE,
    JL,
    JLE,
    JMP,
    JNA,
    JNAE,
    JNB,
    JNBE,
    JNE,
    JNG,
    JNGE,
    JNL,
    JNLE,
    JNZ,
    JZ,
    LEA,
    MOV,
    MOVZX,
    MUL,
    POP,
    POPF,
    PUSH,
    PUSHF,
    RET,
    OR,
    SUB,
    SYSCALL,
    XOR
)

fun findInstruction(instr: AsmInstruction): Pair<Instruction, InstructionVariant?>? {
    val instrName = instr.instructionName.id.text
    val instrOperands = mutableListOf<OpKind>()

    for (arg in instr.instructionArgList.instructionArgList) {
        if (arg.imm != null) instrOperands.add(Imm)
        else if (arg.mem != null) instrOperands.add(Mem)
        else if (arg.reg != null) {
            val reg = findReg(arg.reg!!.id.text)?.kind
            if (reg != null) instrOperands.add(Reg(reg))
            else return null
        } else if (arg.indirection != null) {
            instrOperands.add(Indirection)
        } else {
            error("Unknown instruction arg type")
        }
    }

    for (ins in INSTRUCTIONS) {
        if (!instrName.startsWith(ins.mnemonic)) continue

        val suffix = instrName.removePrefix(ins.mnemonic)

        if (ins.variants != null) for (variant in ins.variants.list) {
            if (variant.matchedBy(instrOperands, suffix)) {
                return ins to variant
            }
        }
        else {
            if (suffix == "" && instrOperands.isEmpty()) {
                return ins to null
            }
        }
    }

    return null
}