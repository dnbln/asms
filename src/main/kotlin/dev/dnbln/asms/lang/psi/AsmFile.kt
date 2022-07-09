package dev.dnbln.asms.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.Key
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.*
import dev.dnbln.asms.lang.AsmFileType
import dev.dnbln.asms.lang.AsmLang
import dev.dnbln.asms.lang.codeInsight.AsmScopeProcessor

class AsmFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, AsmLang) {
    val items: List<AsmItem>
        get() = childrenOfType()

    companion object {
        val RESOLVE_RESULT_CACHE_KEY = Key.create<CachedValue<ResolveState>>("asm.file.cache.resolve.state")
    }

    override fun getFileType(): FileType = AsmFileType

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (item in items) {
            if (item === lastParent) continue

            if (!item.processDeclarations(processor, state, lastParent, place)) return false
        }

        return true
    }

    fun resolveDeclarations(): ResolveState = CachedValuesManager.getCachedValue(
        this,
        RESOLVE_RESULT_CACHE_KEY
    ) {
        val resolveState = ResolveState()
        resolveState.put(AsmScopeProcessor.DEFINED_NAMES_MAP, mutableMapOf())

        processDeclarations(AsmScopeProcessor(), resolveState, null, this)

        return@getCachedValue CachedValueProvider.Result(resolveState, PsiModificationTracker.MODIFICATION_COUNT)
    }
}