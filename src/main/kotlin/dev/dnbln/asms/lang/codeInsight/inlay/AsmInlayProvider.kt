package dev.dnbln.asms.lang.codeInsight.inlay

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.ImmediateConfigurable.Case
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import dev.dnbln.asms.lang.codeInsight.findInstruction
import dev.dnbln.asms.lang.psi.AsmInstruction
import dev.dnbln.asms.lang.psi.AsmInstructionArg
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JPanel


@Suppress("UnstableApiUsage")
class AsmInlayProvider : InlayHintsProvider<AsmInlayProvider.Settings> {
    data class Settings(
        var instructions: Boolean = true
    )

    override val key: SettingsKey<Settings>
        get() = KEY
    override val name: String
        get() = "Asm inlay hints"
    override val previewText: String
        get() = """
            cmp $1, %rdi
            je good
        bad:
            movq $0, %rax
            ret
        good:
            movq $1, %rax
            ret
        """.trimIndent()

    override fun createSettings(): Settings = Settings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: Settings,
        sink: InlayHintsSink
    ): InlayHintsCollector = object : FactoryInlayHintsCollector(editor) {
        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            when (element) {
                is AsmInstruction -> presentForInstruction(element, editor, sink)
            }

            return true
        }

        private fun present(presentation: InlayPresentation): InlayPresentation =
            factory.container(
                presentation,
                padding = InlayPresentationFactory.Padding(7, 7, 5, 5),
                background = Color(64, 64, 64, 50),
            )

        private fun presentForInstruction(element: AsmInstruction, editor: Editor, sink: InlayHintsSink) {
            if (!settings.instructions) return

            val instr = findInstruction(element)

            if (instr != null) {
                val (ins, _) = instr

                when (ins.mnemonic) {
                    "cmp" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" cmp "),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "ja", "jg" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text(">")),
                        false
                    )
                    "jae", "jge" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text(">=")),
                        false
                    )
                    "jb", "jl" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("<")),
                        false
                    )
                    "jbe", "jle" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("<=")),
                        false
                    )
                    "je" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("==")),
                        false
                    )
                    "jna", "jng" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("<= [not >]")),
                        false
                    )
                    "jnae", "jnge" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("< [not >=]")),
                        false
                    )
                    "jnb", "jnl" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text(">= [not <]")),
                        false
                    )
                    "jnbe", "jnle" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("> [not <=]")),
                        false
                    )
                    "jne" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("!=")),
                        false
                    )
                    "jnz" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("!= 0")),
                        false
                    )
                    "jz" -> sink.addInlineElement(
                        element.instructionArgList.endOffset,
                        true,
                        present(factory.text("== 0")),
                        false
                    )
                    "mov" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" = "),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "movzx" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" = ZX("),
                                    presentInstructionArg(left),
                                    factory.text(")")
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "lea" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" = &"),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "add" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" += "),
                                    presentInstructionArg(left)
                                )
                            )


                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "sub" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" -= "),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "and" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" &= "),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "xor" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" ^= "),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                    "or" -> {
                        val (left, right) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(right),
                                    factory.text(" |= "),
                                    presentInstructionArg(left)
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }

                    "call" -> {
                        val (func) = element.instructionArgList.instructionArgList

                        val presentation =
                            present(
                                factory.seq(
                                    presentInstructionArg(func),
                                    factory.text(" ()"),
                                )
                            )

                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            presentation,
                            false
                        )
                    }
                }
            }
        }

        private fun presentInstructionArg(arg: AsmInstructionArg): InlayPresentation = if (arg.reg != null) {
            factory.text(arg.reg!!.text)
        } else if (arg.imm != null) {
            factory.text(arg.imm!!.text)
        } else if (arg.mem != null) {
            factory.text(arg.mem!!.text)
        } else if (arg.indirection != null) {
            factory.text(arg.indirection!!.text)
        } else {
            error("Unknown arg kind")
        }
    }

    override fun createConfigurable(settings: Settings): ImmediateConfigurable =
        object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = JPanel()

            override val cases: List<Case>
                get() = listOf(
                    Case("Show for instructions", "instructions", settings::instructions)
                )
        }

    companion object {
        private val KEY: SettingsKey<Settings> = SettingsKey("asm.inlays")
    }
}