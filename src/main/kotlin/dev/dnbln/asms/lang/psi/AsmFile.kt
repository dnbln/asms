package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import dev.dnbln.asms.lang.AsmFileType
import dev.dnbln.asms.lang.AsmLang

class AsmFile(viewProvider: FileViewProvider): PsiFileBase(viewProvider, AsmLang) {
    override fun getFileType(): FileType = AsmFileType
}