package dev.dnbln.asms.lang.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.childrenOfType
import dev.dnbln.asms.lang.AsmLang

fun createLabelRef(project: Project, name: String): AsmLabelRef =
    createFile(project, "mov $${name}, x").childrenOfType<AsmLabelRef>()[0]

fun createPossiblyDotId(project: Project, name: String): AsmPossiblyDotId =
    createFile(project, "mov $${name}, x").childrenOfType<AsmPossiblyDotId>()[0]

internal fun createFile(project: Project, text: String): AsmFile =
    PsiFileFactory.getInstance(project).createFileFromText("dummy.asm", AsmLang, text) as AsmFile