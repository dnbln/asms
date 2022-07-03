package dev.dnbln.asms.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import dev.dnbln.asms.lang.codeInsight.DirectiveError
import dev.dnbln.asms.lang.codeInsight.findDirective
import dev.dnbln.asms.lang.psi.AsmDirective
import dev.dnbln.asms.lang.psi.AsmVisitor

class AsmDirectiveErrors : LocalInspectionTool() {
    companion object {
        // https://sourceware.org/binutils/docs/as/Pseudo-Ops.html#Pseudo-Ops
        private val KNOWN_DIRECTIVES = setOf(
            "ABORT",
            "abort",
            "align",
            "altmacro",
            "ascii",
            "asciz",
            "attach_to_group",
            "balign",
            "balignw",
            "balignl",
            "bss",
            "byte",

            // ===========================
            // ===       CFI           ===
            // ===========================
            "cfi_sections",
            "cfi_startproc",
            "cfi_endproc",
            "cfi_personality",
            "cfi_personality_id",
            "cfi_fde_data",
            "cfi_lsda",
            "cfi_inline_lsda",
            "cfi_def_cfa",
            "cfi_def_cfa_register",
            "cfi_def_cfa_offset",
            "cfi_adjust_cfa_offset",
            "cfi_offset",
            "cfi_val_offset",
            "cfi_rel_offset",
            "cfi_register",
            "cfi_restore",
            "cfi_undefined",
            "cfi_same_value",
            "cfi_remember_state",
            "cfi_restore_state",
            "cfi_return_column",
            "cfi_signal_frame",
            "cfi_window_save",
            "cfi_escape",
            "cfi_val_encoded_addr",

            "comm",
            "data",
            "dc",
            "dc.a",
            "dc.b",
            "dc.d",
            "dc.l",
            "dc.s",
            "dc.w",
            "dc.x",
            "dcb",
            "dcb.b",
            "dcb.d",
            "dcb.l",
            "dcb.s",
            "dcb.w",
            "dcb.x",
            "ds",
            "ds.b",
            "ds.d",
            "ds.l",
            "ds.p",
            "ds.s",
            "ds.w",
            "ds.x",
            "def",
            "desc",
            "dim",
            "double",
            "eject",
            "else",
            "elseif",
            "end",
            "endef",
            "endfunc",
            "endif",
            "equ",
            "equiv",
            "eqv",
            "err",
            "error",
            "exitm",
            "extern",
            "fail",
            "file",
            "fill",
            "float",
            "func",
            "global",
            "globl",
            "gnu_attribute",
            "hidden",
            "hword",
            "ident",
            "if",
            "incbin",
            "include",
            "int",
            "internal",
            "irp",
            "irpc",
            "lcomm",
            "lflags",
            "line",
            "linkonce",
            "list",
            "ln",
            "loc",
            "loc_mark_labels",
            "local",
            "long",
            "macro",
            "mri",
            "noaltmacro",
            "nolist",
            "nop",
            "nops",
            "octa",
            "offset",
            "org",
            "p2align",
            "p2alignw",
            "p2alignl",
            "popsection",
            "previous",
            "print",
            "protected",
            "psize",
            "purgem",
            "pushsection",
            "quad",
            "reloc",
            "rept",
            "sbttl",
            "scl",
            "section",
            "set",
            "short",
            "single",
            "size",
            "skip",
            "sleb128",
            "space",
            "stabd",
            "stabn",
            "stabs",
            "string",
            "string8",
            "string16",
            "string32",
            "string64",
            "struct",
            "subsection",
            "symver",
            "tag",
            "text",
            "title",
            "tls_common",
            "type",
            "uleb128",
            "val",
            "version",
            "vtable_entry",
            "vtable_inherit",
            "warning",
            "weak",
            "weakref",
            "word",
            "zero",
            "2byte",
            "4byte",
            "8byte",
        )
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = object : AsmVisitor() {
//        override fun visitDirectiveHead(head: AsmDirectiveHead) {
//            val directiveName = head.directiveName
//
//            if (directiveName !in KNOWN_DIRECTIVES) {
//                holder.registerProblem(head, "Unknown directive")
//            }
//        }

        override fun visitDirective(dir: AsmDirective) {
            val directive = findDirective(dir)

            if (directive == null) {
                holder.registerProblem(dir, "Unknown directive")
                return
            }

            directive.second.onFailure {
                val err = it as? DirectiveError ?: return@onFailure

                holder.registerProblem(err.element, err.displayText)
            }
        }
    }
}