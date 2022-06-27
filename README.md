# `asms`

An IntelliJ plugin for x86 and x86_64 AT&T assembly (for GNU as).

# Motivation

Contrary to popular belief, assembly is still taught in universities
as part of CS degrees.

Current assembly plugins for IntelliJ only support other architectures ([6502](https://plugins.jetbrains.com/plugin/16799-6502-assembly)
and [MC68000](https://plugins.jetbrains.com/plugin/17268-mc68000-assembly-language-support)), or NASM syntax (not saying there's
anything wrong with it but from my experience I've seen AT&T more widely used both in and outside of universities).

The lack of a good assembly plugin makes AT&T assembly a little harder to work with, and so I tried rolling out my own.

# Building the plugin

```bash
./gradlew buildPlugin
```

Or for windows:

```shell
.\gradlew.bat buildPlugin
```

And you will have the plugin .zip in `build/distributions`, which you can `Install from disk` in the plugins
settings page.

# Reporting bugs

## Unknown instruction, but the assembler accepts it

Right now the plugin only recognizes 30 or so *very* common instructions.
I'm still working on adding more but x86 is huge, so please open an issue. If you would like to tackle it yourself,
the file you're looking for is probably [this one](https://github.com/dnbln/asms/blob/trunk/src/main/kotlin/dev/dnbln/asms/lang/codeInsight/Instructions.kt).
If you have to add a new instruction, please keep instructions in the file ordered alphabetically.


