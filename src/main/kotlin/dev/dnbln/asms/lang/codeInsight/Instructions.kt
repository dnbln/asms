package dev.dnbln.asms.lang.codeInsight

import dev.dnbln.asms.lang.codeInsight.OpKind.*
import dev.dnbln.asms.lang.codeInsight.RegisterKind.*
import dev.dnbln.asms.lang.psi.AsmInstruction

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
    val suffix: Suffix? = null
) {
    fun matchedBy(ops: List<OpKind>, suffix: String): Boolean {
        if (suffix != "" && suffix != this.suffix?.suffix)
            return false

        if (this.suffix?.mandatory == true && suffix == "")
            return false

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

data class Instruction(
    val mnemonic: String,
    val variants: InstructionVariants? = null,
    val memSafeAlways: Boolean = false
)


val ARITHMETIC_INSTRUCTION_VARIANTS = variants(
    // 8 bit
    InstructionVariant(
        Imm, Reg(Reg8),
        suffix = Suffix("b")
    ),
    InstructionVariant(
        Reg(Reg8), Reg(Reg8),
        suffix = Suffix("b")
    ),
    InstructionVariant(
        Imm, Mem,
        suffix = Suffix("b", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg8), Mem,
        suffix = Suffix("b")
    ),
    InstructionVariant(
        Mem, Reg(Reg8),
        suffix = Suffix("b")
    ),

    // 16 bit
    InstructionVariant(
        Imm, Reg(Reg16),
        suffix = Suffix("w")
    ),
    InstructionVariant(
        Reg(Reg16), Reg(Reg16),
        suffix = Suffix("w")
    ),
    InstructionVariant(
        Imm, Mem,
        suffix = Suffix("w", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg16), Mem,
        suffix = Suffix("w")
    ),
    InstructionVariant(
        Mem, Reg(Reg16),
        suffix = Suffix("w")
    ),

    // 32 bit
    InstructionVariant(
        Imm, Reg(Reg32),
        suffix = Suffix("l")
    ),
    InstructionVariant(
        Reg(Reg32), Reg(Reg32),
        suffix = Suffix("l")
    ),
    InstructionVariant(
        Imm, Mem,
        suffix = Suffix("l", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg32), Mem,
        suffix = Suffix("l")
    ),
    InstructionVariant(
        Mem, Reg(Reg32),
        suffix = Suffix("l")
    ),

    // 64 bit
    InstructionVariant(
        Imm, Reg(Reg64),
        suffix = Suffix("q")
    ),
    InstructionVariant(
        Reg(Reg64), Reg(Reg64),
        suffix = Suffix("q")
    ),
    InstructionVariant(
        Imm, Mem,
        suffix = Suffix("q", mandatory = true)
    ),
    InstructionVariant(
        Reg(Reg64), Mem,
        suffix = Suffix("q")
    ),
    InstructionVariant(
        Mem, Reg(Reg64),
        suffix = Suffix("q")
    ),
)

val ADD = Instruction("add", ARITHMETIC_INSTRUCTION_VARIANTS)
val AND = Instruction("and", ARITHMETIC_INSTRUCTION_VARIANTS)

val CALL = Instruction(
    "call",
    variants(
        InstructionVariant(
            Mem,
            suffix = Suffix("q")
        )
    )
)

val CMP = Instruction(
    "cmp",
    variants(
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
    )
)

val JMP_VARIANTS = variants(
    InstructionVariant(Mem, suffix = Suffix("q")),
    InstructionVariant(Indirection, suffix = Suffix("q")),
    InstructionVariant(Mem, suffix = Suffix("l")),
    InstructionVariant(Indirection, suffix = Suffix("l")),
)

val JA = Instruction("ja", JMP_VARIANTS)
val JAE = Instruction("jae", JMP_VARIANTS)
val JB = Instruction("jb", JMP_VARIANTS)
val JBE = Instruction("jbe", JMP_VARIANTS)
val JE = Instruction("je", JMP_VARIANTS)
val JG = Instruction("jg", JMP_VARIANTS)
val JGE = Instruction("jge", JMP_VARIANTS)
val JL = Instruction("jl", JMP_VARIANTS)
val JLE = Instruction("jle", JMP_VARIANTS)
val JMP = Instruction("jmp", JMP_VARIANTS)
val JNA = Instruction("jna", JMP_VARIANTS)
val JNAE = Instruction("jnae", JMP_VARIANTS)
val JNB = Instruction("jnb", JMP_VARIANTS)
val JNBE = Instruction("jnbe", JMP_VARIANTS)
val JNE = Instruction("jne", JMP_VARIANTS)
val JNG = Instruction("jng", JMP_VARIANTS)
val JNGE = Instruction("jnge", JMP_VARIANTS)
val JNL = Instruction("jnl", JMP_VARIANTS)
val JNLE = Instruction("jnle", JMP_VARIANTS)
val JNZ = Instruction("jnz", JMP_VARIANTS)
val JZ = Instruction("jz", JMP_VARIANTS)

val LEA = Instruction(
    "lea",
    variants(
        InstructionVariant(
            Mem, Reg(Reg32),
            suffix = Suffix("l")
        ),
        InstructionVariant(
            Mem, Reg(Reg64),
            suffix = Suffix("q")
        ),
    ),
    memSafeAlways = true
)

val MOV = Instruction(
    "mov",
    variants(
        // 8 bit move
        InstructionVariant(
            Imm, Reg(Reg8),
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Imm, Mem,
            suffix = Suffix("b", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg8), Reg(Reg8),
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Reg(Reg8), Mem,
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Mem, Reg(Reg8),
            suffix = Suffix("b")
        ),

        // 16 bit move
        InstructionVariant(
            Imm, Reg(Reg16),
            suffix = Suffix("w")
        ),
        InstructionVariant(
            Imm, Mem,
            suffix = Suffix("w", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg16), Reg(Reg16),
            suffix = Suffix("w")
        ),
        InstructionVariant(
            Reg(Reg16), Mem,
            suffix = Suffix("w")
        ),
        InstructionVariant(
            Mem, Reg(Reg16),
            suffix = Suffix("w")
        ),

        // 32 bit move
        InstructionVariant(
            Imm, Reg(Reg32),
            suffix = Suffix("l")
        ),
        InstructionVariant(
            Imm, Mem,
            suffix = Suffix("l", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg32), Reg(Reg32),
            suffix = Suffix("l")
        ),
        InstructionVariant(
            Reg(Reg32), Mem,
            suffix = Suffix("l")
        ),
        InstructionVariant(
            Mem, Reg(Reg32),
            suffix = Suffix("l")
        ),

        // 64 bit move
        InstructionVariant(
            Imm, Reg(Reg64),
            suffix = Suffix("q")
        ),
        InstructionVariant(
            Imm, Mem,
            suffix = Suffix("q", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg64), Reg(Reg64),
            suffix = Suffix("q")
        ),
        InstructionVariant(
            Reg(Reg64), Mem,
            suffix = Suffix("q")
        ),
        InstructionVariant(
            Mem, Reg(Reg64),
            suffix = Suffix("q")
        )
    )
)

val MOVZX = Instruction(
    "movzx",
    variants(
        InstructionVariant(
            Reg(Reg8), Reg(Reg16),
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Mem, Reg(Reg16),
            suffix = Suffix("b", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg8), Reg(Reg32),
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Mem, Reg(Reg32),
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Reg(Reg8), Reg(Reg64),
            suffix = Suffix("b")
        ),
        InstructionVariant(
            Mem, Reg(Reg64),
            suffix = Suffix("b", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg16), Reg(Reg32),
            suffix = Suffix("w")
        ),
        InstructionVariant(
            Mem, Reg(Reg32),
            suffix = Suffix("w", mandatory = true)
        ),
        InstructionVariant(
            Reg(Reg16), Reg(Reg64),
            suffix = Suffix("w")
        ),
        InstructionVariant(
            Mem, Reg(Reg64),
            suffix = Suffix("w", mandatory = true)
        )
    )
)

val POP = Instruction(
    "pop",
    variants(
        InstructionVariant(suffix = Suffix("q")),
        InstructionVariant(
            Reg(Reg64),
            suffix = Suffix("q")
        ),
        InstructionVariant(
            Mem,
            suffix = Suffix("q", mandatory = true)
        )
    )
)
val POPF = Instruction(
    "popf",
    variants(
        InstructionVariant(suffix = Suffix("q"))
    )
)
val PUSH = Instruction(
    "push",
    variants(
        InstructionVariant(
            Imm,
            suffix = Suffix("q")
        ),
        InstructionVariant(
            Reg(Reg64),
            suffix = Suffix("q")
        ),
        InstructionVariant(
            Mem,
            suffix = Suffix("q", mandatory = true)
        )
    ),
)
val PUSHF = Instruction(
    "pushf",
    variants(
        InstructionVariant(suffix = Suffix("q"))
    )
)

val OR = Instruction("or", ARITHMETIC_INSTRUCTION_VARIANTS)
val SUB = Instruction("sub", ARITHMETIC_INSTRUCTION_VARIANTS)
val SYSCALL = Instruction("syscall")
val XOR = Instruction("xor", ARITHMETIC_INSTRUCTION_VARIANTS)

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
    POP,
    POPF,
    PUSH,
    PUSHF,
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
            if (reg != null)
                instrOperands.add(Reg(reg))
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

        if (ins.variants != null)
            for (variant in ins.variants.list) {
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