package dev.dnbln.asms.lang.codeInsight

import dev.dnbln.asms.lang.codeInsight.RegisterKind.*

sealed class RegisterKind {
    object Reg8 : RegisterKind() {
        override fun toString(): String = "REG8"
    }
    object Reg16 : RegisterKind() {
        override fun toString(): String = "REG16"
    }
    object Reg32 : RegisterKind() {
        override fun toString(): String = "REG32"
    }
    object Reg64 : RegisterKind() {
        override fun toString(): String = "REG64"
    }

    object Reg80 : RegisterKind() {
        override fun toString(): String = "REG80"
    }
    object Reg128 : RegisterKind() {
        override fun toString(): String = "REG128"
    }
    object Reg256 : RegisterKind() {
        override fun toString(): String = "REG256"
    }
    object Reg512 : RegisterKind() {
        override fun toString(): String = "REG512"
    }
}

data class Register(val name: String, val kind: RegisterKind)

val REGISTERS = setOf(
    Register("rax", Reg64),
    Register("eax", Reg32),
    Register("ax", Reg16),
    Register("al", Reg8),
    Register("ah", Reg8),

    Register("rbx", Reg64),
    Register("ebx", Reg32),
    Register("bx", Reg16),
    Register("bl", Reg8),
    Register("bh", Reg8),

    Register("rcx", Reg64),
    Register("ecx", Reg32),
    Register("cx", Reg16),
    Register("cl", Reg8),
    Register("ch", Reg8),

    Register("rdx", Reg64),
    Register("edx", Reg32),
    Register("dx", Reg16),
    Register("dl", Reg8),
    Register("dh", Reg8),

    Register("rbp", Reg64),
    Register("ebp", Reg32),
    Register("bp", Reg16),
    Register("bpl", Reg8),

    Register("rsi", Reg64),
    Register("esi", Reg32),
    Register("si", Reg16),
    Register("sil", Reg8),

    Register("rdi", Reg64),
    Register("edi", Reg32),
    Register("di", Reg16),
    Register("dil", Reg8),

    Register("rsp", Reg64),
    Register("esp", Reg32),
    Register("sp", Reg16),
    Register("spl", Reg8),

    Register("r8", Reg64),
    Register("r8d", Reg32),
    Register("r8w", Reg16),
    Register("r8b", Reg8),


    Register("r8", Reg64),
    Register("r8d", Reg32),
    Register("r8w", Reg16),
    Register("r8b", Reg8),

    Register("r9", Reg64),
    Register("r9d", Reg32),
    Register("r9w", Reg16),
    Register("r9b", Reg8),

    Register("r10", Reg64),
    Register("r10d", Reg32),
    Register("r10w", Reg16),
    Register("r10b", Reg8),

    Register("r11", Reg64),
    Register("r11d", Reg32),
    Register("r11w", Reg16),
    Register("r11b", Reg8),

    Register("r12", Reg64),
    Register("r12d", Reg32),
    Register("r12w", Reg16),
    Register("r12b", Reg8),

    Register("r13", Reg64),
    Register("r13d", Reg32),
    Register("r13w", Reg16),
    Register("r13b", Reg8),

    Register("r14", Reg64),
    Register("r14d", Reg32),
    Register("r14w", Reg16),
    Register("r14b", Reg8),

    Register("r15", Reg64),
    Register("r15d", Reg32),
    Register("r15w", Reg16),
    Register("r15b", Reg8),

    Register("cr0", Reg64),
    Register("cr1", Reg64),
    Register("cr2", Reg64),
    Register("cr3", Reg64),
    Register("cr4", Reg64),
    Register("cr5", Reg64),
    Register("cr6", Reg64),
    Register("cr7", Reg64),
    Register("cr8", Reg64),
    Register("cr9", Reg64),
    Register("cr10", Reg64),
    Register("cr11", Reg64),
    Register("cr12", Reg64),
    Register("cr13", Reg64),
    Register("cr14", Reg64),
    Register("cr15", Reg64),


    Register("msw", Reg64),
    Register("mxcsr", Reg32),

    Register("dr0", Reg64),
    Register("dr1", Reg64),
    Register("dr2", Reg64),
    Register("dr3", Reg64),
    Register("dr4", Reg64),
    Register("dr5", Reg64),
    Register("dr6", Reg64),
    Register("dr7", Reg64),
    Register("dr8", Reg64),
    Register("dr9", Reg64),
    Register("dr10", Reg64),
    Register("dr11", Reg64),
    Register("dr12", Reg64),
    Register("dr13", Reg64),
    Register("dr14", Reg64),
    Register("dr15", Reg64),

    Register("cs", Reg16),
    Register("ss", Reg16),
    Register("ds", Reg16),
    Register("es", Reg16),
    Register("fs", Reg16),
    Register("gs", Reg16),


    Register("gdtr", Reg16),
    Register("idtr", Reg16),
    Register("tr", Reg16),
    Register("ldtr", Reg16),


    Register("rflags", Reg64),
    Register("eflags", Reg32),
    Register("flags", Reg16),

    Register("cw", Reg16),
    Register("fp_cs", Reg16),
    Register("sw", Reg16),
    Register("tw", Reg16),
    Register("fp_ds", Reg16),
    Register("fp_opc", Reg16),
    Register("fp_dp", Reg32),
    Register("fp_ip", Reg32),

    Register("zmm0", Reg512),
    Register("ymm0", Reg256),
    Register("xmm0", Reg128),

    Register("zmm1", Reg512),
    Register("ymm1", Reg256),
    Register("xmm1", Reg128),

    Register("zmm2", Reg512),
    Register("ymm2", Reg256),
    Register("xmm2", Reg128),

    Register("zmm3", Reg512),
    Register("ymm3", Reg256),
    Register("xmm3", Reg128),

    Register("zmm4", Reg512),
    Register("ymm4", Reg256),
    Register("xmm4", Reg128),

    Register("zmm5", Reg512),
    Register("ymm5", Reg256),
    Register("xmm5", Reg128),

    Register("zmm6", Reg512),
    Register("ymm6", Reg256),
    Register("xmm6", Reg128),

    Register("zmm7", Reg512),
    Register("ymm7", Reg256),
    Register("xmm7", Reg128),

    Register("zmm8", Reg512),
    Register("ymm8", Reg256),
    Register("xmm8", Reg128),

    Register("zmm9", Reg512),
    Register("ymm9", Reg256),
    Register("xmm9", Reg128),

    Register("zmm10", Reg512),
    Register("ymm10", Reg256),
    Register("xmm10", Reg128),

    Register("zmm11", Reg512),
    Register("ymm11", Reg256),
    Register("xmm11", Reg128),

    Register("zmm12", Reg512),
    Register("ymm12", Reg256),
    Register("xmm12", Reg128),

    Register("zmm13", Reg512),
    Register("ymm13", Reg256),
    Register("xmm13", Reg128),

    Register("zmm14", Reg512),
    Register("ymm14", Reg256),
    Register("xmm14", Reg128),

    Register("zmm15", Reg512),
    Register("ymm15", Reg256),
    Register("xmm15", Reg128),

    Register("zmm16", Reg512),
    Register("zmm17", Reg512),
    Register("zmm18", Reg512),
    Register("zmm19", Reg512),
    Register("zmm20", Reg512),
    Register("zmm21", Reg512),
    Register("zmm22", Reg512),
    Register("zmm23", Reg512),
    Register("zmm24", Reg512),
    Register("zmm25", Reg512),
    Register("zmm26", Reg512),
    Register("zmm27", Reg512),
    Register("zmm28", Reg512),
    Register("zmm29", Reg512),
    Register("zmm30", Reg512),
    Register("zmm31", Reg512),

    Register("st0", Reg80),
    Register("mm0", Reg64),

    Register("st1", Reg80),
    Register("mm1", Reg64),

    Register("st2", Reg80),
    Register("mm2", Reg64),

    Register("st3", Reg80),
    Register("mm3", Reg64),

    Register("st4", Reg80),
    Register("mm4", Reg64),

    Register("st5", Reg80),
    Register("mm5", Reg64),

    Register("st6", Reg80),
    Register("mm6", Reg64),

    Register("st7", Reg80),
    Register("mm7", Reg64),
)

fun findReg(name: String): Register? = REGISTERS.find { it.name == name }