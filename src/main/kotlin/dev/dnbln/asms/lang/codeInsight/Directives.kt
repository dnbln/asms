@file:Suppress("UnstableApiUsage")

package dev.dnbln.asms.lang.codeInsight

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.applyIf
import dev.dnbln.asms.lang.psi.*

sealed class DirectiveError : RuntimeException() {
    abstract val displayText: String

    class WrongNumberOfArgs(val dir: AsmDirective, private val description: String) : DirectiveError() {
        override val displayText: String
            get() = description
    }

    class WrongArgType(val arg: AsmDirectiveArg, private val expected: String, private val actual: String = arg.kind) :
        DirectiveError() {
        override val displayText: String
            get() = "Wrong argument type; expected $expected, actual $actual"
    }
}

interface AsmDirectiveArgsSink {
    fun addDeclaration(dir: AsmDirective, nameArg: AsmDirectiveArgName)
    fun addReference(dir: AsmDirective, nameArg: AsmDirectiveArgName)
}

@Suppress("UnstableApiUsage")
class AsmDirectiveArgsCollector(private val symbols: MutableCollection<PsiSymbolDeclaration>) : AsmDirectiveArgsSink {
    override fun addDeclaration(dir: AsmDirective, nameArg: AsmDirectiveArgName) {
        symbols.add(object : PsiSymbolDeclaration {
            override fun getDeclaringElement(): PsiElement = dir

            override fun getRangeInDeclaringElement(): TextRange = nameArg.rangeIn(dir)

            override fun getSymbol(): Symbol = nameArg
        })
    }

    override fun addReference(dir: AsmDirective, nameArg: AsmDirectiveArgName) {}
}

sealed class Directive<DirectiveArgs> {
    abstract fun directiveCheckName(dir: AsmDirective): Boolean
    abstract fun directiveCheck(dir: AsmDirective): Result<DirectiveArgs>
    abstract fun collectArgs(dir: AsmDirective, args: DirectiveArgs, argsSink: AsmDirectiveArgsSink)

    open fun definesSymbol(symbolName: String, args: DirectiveArgs): AsmDirectiveArgName? = null
}

abstract class SimpleNamedDirective<DirectiveArgs>(vararg val names: String) : Directive<DirectiveArgs>() {
    override fun directiveCheckName(dir: AsmDirective): Boolean =
        names.any { it == dir.directiveCode.directiveHead.directiveName }
}

data class LcommArgs(val name: String, val size: Int, val nameArg: AsmDirectiveArgName, val sizeArg: AsmNum)

object Lcomm : SimpleNamedDirective<LcommArgs>("lcomm") {
    override fun directiveCheck(dir: AsmDirective): Result<LcommArgs> {
        val args =
            dir.directiveCode.directiveArgs ?: return Result.failure(
                DirectiveError.WrongNumberOfArgs(
                    dir,
                    "No arguments, expected 2"
                )
            )
        val argList = args.directiveArgList

        if (argList.size != 2) return Result.failure(
            DirectiveError.WrongNumberOfArgs(
                dir,
                "Wrong number of arguments; expected 2, found ${argList.size}"
            )
        )

        val (nameArgRaw, sizeArgRaw) = argList

        val nameArg =
            nameArgRaw.directiveArgName ?: return Result.failure(DirectiveError.WrongArgType(nameArgRaw, "name"))
        val name = nameArg.name!!

        val sizeArg = sizeArgRaw.num ?: return Result.failure(DirectiveError.WrongArgType(sizeArgRaw, "num"))
        val size = sizeArg.value

        return Result.success(LcommArgs(name, size, nameArg, sizeArg))
    }

    override fun collectArgs(dir: AsmDirective, args: LcommArgs, argsSink: AsmDirectiveArgsSink) {
        argsSink.addDeclaration(dir, args.nameArg)
    }

    override fun definesSymbol(symbolName: String, args: LcommArgs): AsmDirectiveArgName? {
        if (args.name == symbolName) return args.nameArg

        return null
    }
}

data class GlobalArgs(val name: String, val nameArg: AsmDirectiveArgName)

object Global : SimpleNamedDirective<GlobalArgs>("global", "globl") {
    override fun directiveCheck(dir: AsmDirective): Result<GlobalArgs> {
        val args =
            dir.directiveCode.directiveArgs ?: return Result.failure(
                DirectiveError.WrongNumberOfArgs(
                    dir,
                    "No arguments, expected 2"
                )
            )
        val argList = args.directiveArgList

        if (argList.size != 1) return Result.failure(
            DirectiveError.WrongNumberOfArgs(
                dir,
                "Wrong number of arguments; expected 1, found ${argList.size}"
            )
        )

        val (nameArgRaw) = argList

        val nameArg =
            nameArgRaw.directiveArgName ?: return Result.failure(DirectiveError.WrongArgType(nameArgRaw, "name"))
        val name = nameArg.name!!

        return Result.success(GlobalArgs(name, nameArg))
    }

    override fun collectArgs(dir: AsmDirective, args: GlobalArgs, argsSink: AsmDirectiveArgsSink) {
        argsSink.addReference(dir, args.nameArg)
    }
}

val DIRECTIVES = setOf<Directive<out Any>>(
    Lcomm, Global
)

fun findDirective(directive: AsmDirective): Pair<Directive<out Any>, Result<Any>>? =
    DIRECTIVES.find { it.directiveCheckName(directive) }?.let { it to it.directiveCheck(directive) }

fun <T> Directive<T>.checkAndCollectArgs(
    directive: AsmDirective,
    argsSink: AsmDirectiveArgsSink,
    throwOnFailure: Boolean = false
) {
    if (!directiveCheckName(directive)) return

    directiveCheck(directive)
        .onSuccess { collectArgs(directive, it, argsSink) }
        .applyIf(throwOnFailure) { onFailure { throw it } }
}

fun findDirectiveAndCollectArgs(
    directive: AsmDirective,
    argsSink: AsmDirectiveArgsSink,
    throwOnFailure: Boolean = false
) {
    Lcomm.checkAndCollectArgs(directive, argsSink, throwOnFailure)
    Global.checkAndCollectArgs(directive, argsSink, throwOnFailure)
}