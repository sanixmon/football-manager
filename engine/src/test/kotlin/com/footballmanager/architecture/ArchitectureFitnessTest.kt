package com.footballmanager.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Architectural Fitness Functions (Enforcer).
 *
 * Automatically guards layering invariants, module boundaries, and dependency directions
 * to prevent regressions and accidental coupling in CI builds.
 */
class ArchitectureFitnessTest {

    private fun findEngineSourceDir(): File {
        val candidates = listOf(
            File("engine/src/main/kotlin"),
            File("src/main/kotlin"),
            File("../engine/src/main/kotlin"),
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: error("Could not locate engine source directory in candidates: $candidates")
    }

    private fun checkViolations(
        subDir: String = "",
        ruleName: String,
        forbiddenPrefixes: List<String>,
    ): List<String> {
        val engineDir = findEngineSourceDir()
        val targetDir = if (subDir.isBlank()) engineDir else File(engineDir, subDir)
        if (!targetDir.exists()) return emptyList()

        val violations = mutableListOf<String>()
        targetDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val imports = file.readLines()
                    .map { it.trim() }
                    .filter { it.startsWith("import ") }
                    .map { it.removePrefix("import ").trim().split(" ")[0].trimEnd(';') }

                for (imp in imports) {
                    for (forbidden in forbiddenPrefixes) {
                        if (imp.startsWith(forbidden)) {
                            violations.add("[$ruleName] ${file.name} imports forbidden dependency: $imp")
                        }
                    }
                }
            }
        return violations
    }

    @Test
    fun `engine module must have zero Android and UI framework dependencies`() {
        val violations = checkViolations(
            ruleName = "Engine Purity",
            forbiddenPrefixes = listOf("android.", "androidx.", "com.footballmanager.app.", "java.awt.", "javax.swing.")
        )
        assertTrue(violations.isEmpty(), "Engine purity violated:\n" + violations.joinToString("\n"))
    }

    @Test
    fun `model package must not depend on outer infrastructure, usecases, or modding`() {
        val violations = checkViolations(
            subDir = "com/footballmanager/model",
            ruleName = "Model Purity",
            forbiddenPrefixes = listOf(
                "com.footballmanager.mod.",
                "com.footballmanager.app.",
                "com.footballmanager.repository.",
            )
        )
        assertTrue(violations.isEmpty(), "Model purity violated:\n" + violations.joinToString("\n"))
    }

    @Test
    fun `calculator package must remain pure domain arithmetic without simulation or infrastructure`() {
        val violations = checkViolations(
            subDir = "com/footballmanager/calculator",
            ruleName = "Calculator Purity",
            forbiddenPrefixes = listOf(
                "com.footballmanager.mod.",
                "com.footballmanager.app.",
                "com.footballmanager.repository.",
                "com.footballmanager.simulation.",
                "com.footballmanager.usecase.",
            )
        )
        assertTrue(violations.isEmpty(), "Calculator purity violated:\n" + violations.joinToString("\n"))
    }

    @Test
    fun `usecase layer must not depend on UI, graphics, or modding infrastructure`() {
        val violations = checkViolations(
            subDir = "com/footballmanager/usecase",
            ruleName = "UseCase Isolation",
            forbiddenPrefixes = listOf(
                "com.footballmanager.mod.",
                "com.footballmanager.app.",
                "com.footballmanager.graphics.",
            )
        )
        assertTrue(violations.isEmpty(), "UseCase isolation violated:\n" + violations.joinToString("\n"))
    }

    @Test
    fun `repository layer must remain independent of simulation and usecases`() {
        val violations = checkViolations(
            subDir = "com/footballmanager/repository",
            ruleName = "Repository Isolation",
            forbiddenPrefixes = listOf(
                "com.footballmanager.simulation.",
                "com.footballmanager.usecase.",
                "com.footballmanager.app.",
                "com.footballmanager.mod.",
            )
        )
        assertTrue(violations.isEmpty(), "Repository isolation violated:\n" + violations.joinToString("\n"))
    }

    @Test
    fun `mod layer must not depend on UI or usecase layer`() {
        val violations = checkViolations(
            subDir = "com/footballmanager/mod",
            ruleName = "Mod Layer Isolation",
            forbiddenPrefixes = listOf(
                "com.footballmanager.app.",
                "com.footballmanager.usecase.",
            )
        )
        assertTrue(violations.isEmpty(), "Mod isolation violated:\n" + violations.joinToString("\n"))
    }
}
