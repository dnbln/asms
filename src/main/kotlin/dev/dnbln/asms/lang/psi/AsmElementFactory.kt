package dev.dnbln.asms.lang.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.descendantsOfType
import dev.dnbln.asms.lang.AsmLang

fun createLabelRef(project: Project, name: String): AsmLabelRef =
    createFile(project, "mov $${name}, x").descendantsOfType<AsmLabelRef>().first()

fun createPossiblyDotId(project: Project, name: String): AsmPossiblyDotId =
    createFile(project, "mov $${name}, x").descendantsOfType<AsmPossiblyDotId>().first()

internal fun createFile(project: Project, text: String): AsmFile =
    PsiFileFactory.getInstance(project).createFileFromText("dummy.asm", AsmLang, text) as AsmFile