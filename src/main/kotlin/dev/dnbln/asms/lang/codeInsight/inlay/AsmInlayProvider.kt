package dev.dnbln.asms.lang.codeInsight.inlay

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.ImmediateConfigurable.Case
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
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
class AsmInlayPresentationFactory(val factory: PresentationFactory) : InlayPresentationFactory by factory {
    fun presentInstructionArg(arg: AsmInstructionArg): InlayPresentation = if (arg.reg != null) {
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
                val (ins, variant) = instr

                if (ins.present != null) {
                    val presentation = (ins.present)(element, variant, AsmInlayPresentationFactory(factory))

                    if (presentation != null)
                        sink.addInlineElement(
                            element.instructionArgList.endOffset,
                            true,
                            present(presentation),
                            false
                        )
                }
            }
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