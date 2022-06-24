package dev.dnbln.asms.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object AsmFileType : LanguageFileType(AsmLang) {
    override fun getName(): String = "asm"

    override fun getDescription(): String = "Assembly file"

    override fun getDefaultExtension(): String = "s"

    override fun getIcon(): Icon? = null
}