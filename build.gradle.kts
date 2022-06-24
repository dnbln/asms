import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"

    id("org.jetbrains.grammarkit") version "2021.2.2"
}

group = "dev.dnbln"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2022.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("PsiViewer:221-SNAPSHOT"))
}

tasks {
    val genLexer = withType<GenerateLexerTask> {
        source.set("src/main/grammars/AsmLexer.flex")
        targetDir.set("src/main/gen/dev/dnbln/asms/lang/lexer")
        targetClass.set("AsmLexer")
        purgeOldFiles.set(true)
    }

    val genParser = withType<GenerateParserTask> {
        source.set("src/main/grammars/asm.bnf")
        targetRoot.set("src/main/gen/")
        pathToParser.set("/dev/dnbln/asms/lang/parser/AsmParser.java")
        pathToPsiRoot.set("/dev/dnbln/asms/lang/psi")
        purgeOldFiles.set(true)
    }


    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"

        sourceSets["main"].java.srcDirs("src/main/gen")

        dependsOn(genLexer, genParser)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"

        dependsOn(genLexer, genParser)
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
