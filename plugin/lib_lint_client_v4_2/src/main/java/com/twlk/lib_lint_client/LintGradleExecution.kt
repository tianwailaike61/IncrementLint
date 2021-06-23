/*
 * MIT License
 *
 * Copyright (c) 2021 tianwailaike61
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.twlk.lib_lint_client

import com.android.SdkConstants
import com.android.tools.lint.*
import com.android.tools.lint.LintCliClient.Companion.continueAfterBaseLineCreated
import com.android.tools.lint.LintStats.Companion.create
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.checks.NonAndroidIssueRegistry
import com.android.tools.lint.checks.UnusedResourceDetector
import com.android.tools.lint.client.api.*
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.gradle.KotlinSourceFoldersResolver
import com.android.tools.lint.gradle.api.LintExecutionRequest
import com.android.tools.lint.gradle.api.VariantInputs
import com.android.tools.lint.gradle.createOutputPath
import com.android.tools.lint.gradle.syncTo
import com.android.tools.lint.gradle.validateOutputFile
import com.android.tools.lint.model.LintModelLintOptions
import com.twlk.lib_lint_base.LintResultCollector
import com.twlk.lib_lint_client_v4_2.IncrementLintClient
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.math.max

/**
 * Class responsible for driving lint from within Gradle. The purpose of this class is to isolate
 * all lint API access to this single class, such that Gradle can load this driver in its own class
 * loader and thereby have lint itself run in its own class loader, such that classes in the Gradle
 * plugins (such as the Kotlin compiler) does not interfere with classes used by lint (such as a
 * different bundled version of the Kotlin compiler.)
 */
@Suppress("unused") // Used via reflection from LintExecutionRequest
class LintGradleExecution(
    private val descriptor: LintExecutionRequest, private val collector: LintResultCollector
) {
    // Along with the constructor, the only public access into this class,
    // intended to be used via reflection. Everything else should be private:
    @Throws(IOException::class)
    fun analyze() {
        LintClient.clientName = LintClient.CLIENT_GRADLE
        if (descriptor.android) {
            val variantName = descriptor.variantName
            if (variantName != null) {
                lintSingleVariant(variantName)
            } else { // All variants
                val variantNames = descriptor.getVariantNames()
                if (variantNames.size == 1) { // usually not the case
                    lintSingleVariant(variantNames.iterator().next())
                } else {
                    lintAllVariants(variantNames)
                }
            }
        } else {
            // Not applying the Android Gradle plugin
            lintNonAndroid()
        }
    }

    private val lintOptions: LintModelLintOptions? get() = descriptor.lintOptions
    private val sdkHome: File? get() = descriptor.sdkHome
    private val isFatalOnly: Boolean get() = descriptor.isFatalOnly
    private val reportsDir: File? get() = descriptor.reportsDir

    private fun abort(
        client: IncrementLintClient?,
        incidents: List<Incident>?,
        isAndroid: Boolean
    ) {
        var message: String = if (isAndroid) {
            if (isFatalOnly) {
                """
                Lint found fatal errors while assembling a release target.

                To proceed, either fix the issues identified by lint, or modify your build script as follows:
                ...
                android {
                    lintOptions {
                        checkReleaseBuilds false
                        // Or, if you prefer, you can continue to check for errors in release builds,
                        // but continue the build even when errors are found:
                        abortOnError false
                    }
                }
                ...
                """.trimIndent()
            } else {
                """
                Lint found errors in the project; aborting build.

                Fix the issues identified by lint, or add the following to your build script to proceed with errors:
                ...
                android {
                    lintOptions {
                        abortOnError false
                    }
                }
                ...
                """.trimIndent()
            }
        } else {
            """
            Lint found errors in the project; aborting build.

            Fix the issues identified by lint, or add the following to your build script to proceed with errors:
            ...
            lintOptions {
                abortOnError false
            }
            ...
            """.trimIndent()
        }
        if (incidents != null && client != null && // See if there aren't any text reporters
            client.flags.reporters.none { reporter -> reporter is TextReporter }
        ) {
            var errors = incidents.asSequence()
                .filter { warning -> warning.severity.isError }
                .toList()
            if (errors.isNotEmpty()) {
                val writer = StringWriter()
                if (errors.size > 3) {
                    writer.append("\nThe first 3 errors (out of ${errors.size}) were:\n\n")
                    errors = listOf(errors[0], errors[1], errors[2])
                } else {
                    writer.append("\nErrors found:\n\n")
                }
                val flags = client.flags
                flags.isExplainIssues = false
                val reporter = Reporter.createTextReporter(
                    client, flags, null, writer,
                    close = false
                )
                try {
                    val stats = create(errors.size, 0)
                    reporter.setWriteStats(false)
                    reporter.write(stats, errors)
                    message += writer.toString()
                } catch (ignore: IOException) {
                }
            }
        }
        throw GradleException(message)
    }

    /** Runs lint on the given variant and returns the set of warnings  */
    private fun runLint(
        variantName: String?,
        variantInputs: VariantInputs,
        report: Boolean,
        isAndroid: Boolean,
        allowFix: Boolean,
        dispose: Boolean
    ): Pair<List<Incident>, LintBaseline?> {
        val registry = createIssueRegistry(isAndroid)
        val flags = LintCliFlags()
        val client = IncrementLintClient(
            descriptor.gradlePluginVersion,
            registry,
            flags,
            descriptor.project,
            descriptor.sdkHome,
            variantName,
            variantInputs,
            descriptor.buildToolsRevision,
            KotlinSourceFoldersResolver { name: String, project: Project? ->
                descriptor.getKotlinSourceFolders(name, project)
            },
            isAndroid,
            variantName
        )
        val fatalOnly = descriptor.isFatalOnly
        if (fatalOnly) {
            flags.isFatalOnly = true
        }

        // Explicit fix Gradle target?
        val autoFixing = allowFix and descriptor.autoFix
        val lintOptions = descriptor.lintOptions
        if (lintOptions != null) {
            // IDEA: Find out if we're on a CI server (how? $DISPLAY available etc?)
            // and if so turn off auto-suggest. Other clues include setting
            // temp dir etc.
            syncOptions(
                lintOptions,
                client,
                flags,
                variantName,
                descriptor.project,
                descriptor.reportsDir,
                report,
                fatalOnly,
                allowFix
            )

            if (!isAndroid) {
                // We won't be able to load a project model here so use an override
                // configuration instead
                val configurations = client.configurations
                val override = LintOptionsConfiguration(configurations, lintOptions, fatalOnly)
                val lintConfig = lintOptions.lintConfig
                val fallback = if (lintConfig != null && lintConfig.exists())
                    LintXmlConfiguration.create(configurations, lintConfig)
                else
                    null
                configurations.addGlobalConfigurations(fallback = fallback, override = override)
            }
        } else {
            // Set up some default reporters
            flags.reporters.add(
                Reporter.createTextReporter(
                    client, flags, null, PrintWriter(System.out, true),
                    close = false
                )
            )
            if (!autoFixing) {
                val html =
                    validateOutputFile(
                        createOutputPath(
                            descriptor.project,
                            null,
                            ".html",
                            null,
                            flags.isFatalOnly
                        )
                    )
                val xml =
                    validateOutputFile(
                        createOutputPath(
                            descriptor.project,
                            null,
                            SdkConstants.DOT_XML,
                            null,
                            flags.isFatalOnly
                        )
                    )
                try {
                    flags.reporters.add(Reporter.createHtmlReporter(client, html, flags))
                    flags.reporters
                        .add(
                            Reporter.createXmlReporter(
                                client,
                                xml,
                                intendedForBaseline = false,
                                includeFixes = flags.isIncludeXmlFixes
                            )
                        )
                } catch (e: IOException) {
                    throw GradleException(e.message ?: e.toString(), e)
                }
            }
        }
        if (!report || fatalOnly) {
            flags.isQuiet = true
        }
        flags.isWriteBaselineIfMissing = report && !fatalOnly && !autoFixing
        val warnings: Pair<List<Incident>, LintBaseline?>
        if (autoFixing) {
            flags.isAutoFix = true
            flags.isSetExitCode = false
        }
        try {
            warnings = client.run(collector)
        } catch (e: IOException) {
            throw GradleException("Invalid arguments.", e)
        }
        if (dispose) {
            client.disposeProjects(emptyList())
        }
        if (report && client.haveErrors() && flags.isSetExitCode) {
            abort(client, warnings.first, isAndroid)
        }
        return warnings
    }

    /** Runs lint on a single specified variant  */
    private fun lintSingleVariant(variantName: String) {
        val variantInputs = descriptor.getVariantInputs(variantName) ?: return
        runLint(
            variantName = variantName,
            variantInputs = variantInputs,
            report = true,
            isAndroid = true,
            allowFix = true,
            dispose = true
        )
    }

    /**
     * Runs lint for a non-Android project (such as a project that only applies the Kotlin Gradle
     * plugin, not the Android Gradle plugin
     */
    private fun lintNonAndroid() {
        val variantInputs = descriptor.getVariantInputs("") ?: return
        runLint(
            variantName = null,
            variantInputs = variantInputs,
            report = true,
            isAndroid = false,
            allowFix = true,
            dispose = true
        )
    }

    /**
     * Runs lint individually on all the variants, and then compares the results across variants and
     * reports these
     */
    private fun lintAllVariants(variantNames: Set<String>) {
        // In the Gradle integration we iterate over each variant, and
        // attribute unused resources to each variant, so don't make
        // each variant run go and inspect the inactive variant sources
        UnusedResourceDetector.sIncludeInactiveReferences = false
        val warningMap: MutableMap<String, List<Incident>> = mutableMapOf()
        val baselines: MutableList<LintBaseline> = mutableListOf()
        var first = true
        for (variantName in variantNames) {
            // we are not running lint on all the variants, so skip the ones where we don't have
            // a variant inputs (see TaskManager::isLintVariant)
            val variantInputs = descriptor.getVariantInputs(variantName)
            if (variantInputs != null) {
                val pair = runLint(
                    variantName = variantName,
                    variantInputs = variantInputs,
                    report = false,
                    isAndroid = true,
                    allowFix = first,
                    dispose = false
                )
                first = false
                val warnings = pair.first
                warningMap[variantName] = warnings
                val baseline = pair.second
                if (baseline != null) {
                    baselines.add(baseline)
                }
            }
        }

        val lintOptions = lintOptions

        // Compute error matrix
        var quiet = false
        if (lintOptions != null) {
            quiet = lintOptions.quiet
        }
        for ((variantName, warnings) in warningMap) {
            if (!isFatalOnly && !quiet) {
                descriptor.warn(
                    "Ran lint on variant {}: {} issues found",
                    variantName,
                    warnings.size
                )
            }
        }

        val mergedWarnings = mergeWarnings(warningMap, variantNames)
        val stats = create(mergedWarnings, baselines)
        val errorCount = stats.errorCount

        // We pick the first variant to generate the full report and don't generate if we don't
        // have any variants.
        if (variantNames.isNotEmpty()) {
            val allVariants: MutableSet<String> = Sets.newTreeSet()
            allVariants.addAll(variantNames)
            val variantName = allVariants.iterator().next()
            val registry: IssueRegistry = BuiltinIssueRegistry()
            val flags = LintCliFlags()
            val variantInputs = descriptor.getVariantInputs(variantName)
                ?: error(variantName)
            val client = IncrementLintClient(
                descriptor.gradlePluginVersion,
                registry,
                flags,
                descriptor.project,
                sdkHome,
                variantName,
                variantInputs,
                descriptor.buildToolsRevision,
                KotlinSourceFoldersResolver { name: String, project: Project? ->
                    descriptor.getKotlinSourceFolders(name, project)
                },
                true,
                if (isFatalOnly) LintBaseline.VARIANT_FATAL else LintBaseline.VARIANT_ALL
            )

            syncOptions(
                lintOptions,
                client,
                flags,
                null,
                descriptor.project,
                reportsDir,
                true,
                isFatalOnly,
                true
            )

            // When running the individual variant scans we turn off auto fixing
            // so perform it manually here when we have the merged results
            if (flags.isAutoFix) {
                flags.isSetExitCode = false
                LintFixPerformer(client, !flags.isQuiet).fix(mergedWarnings)
            }
            for (reporter in flags.reporters) {
                reporter.write(stats, mergedWarnings)
            }
            val baselineFile = flags.baselineFile
            if (baselineFile != null && !baselineFile.exists() && !flags.isAutoFix) {
                val dir = baselineFile.parentFile
                var ok = true
                if (!dir.isDirectory) {
                    ok = dir.mkdirs()
                }
                if (!ok) {
                    System.err.println("Couldn't create baseline folder $dir")
                } else {
                    val reporter = Reporter.createXmlReporter(
                        client,
                        baselineFile,
                        intendedForBaseline = true,
                        includeFixes = false
                    )
                    reporter.setBaselineAttributes(
                        client,
                        if (flags.isFatalOnly) LintBaseline.VARIANT_FATAL else LintBaseline.VARIANT_ALL
                    )
                    reporter.write(stats, mergedWarnings)
                    System.err.println("Created baseline file $baselineFile")
                    if (continueAfterBaseLineCreated()) {
                        return
                    }
                    System.err.println("(Also breaking build in case this was not intentional.)")
                    val message = client.getBaselineCreationMessage(baselineFile)
                    throw GradleException(message)
                }
            }
            val firstBaseline = if (baselines.isEmpty()) null else baselines[0]
            if (baselineFile != null && firstBaseline != null) {
                client.emitBaselineDiagnostics(firstBaseline, baselineFile, stats)
            }
            if (flags.isSetExitCode && errorCount > 0) {
                abort(client, mergedWarnings, true)
            }
        }
    }

    /**
     * Given a list of results from separate variants, merge them into a single list of warnings,
     * and mark their
     *
     * @param warningMap a map from variant to corresponding warnings
     * @param allVariants all the possible variants in the project
     * @return a merged list of issues
     */
    private fun mergeWarnings(
        warningMap: Map<String, List<Incident>>,
        allVariants: Set<String>
    ): List<Incident> {
        // Easy merge?
        if (warningMap.size == 1) {
            return warningMap.values.first()
        }
        var maxCount = 0
        for (warnings in warningMap.values) {
            val size = warnings.size
            maxCount = max(size, maxCount)
        }
        if (maxCount == 0) {
            return emptyList()
        }
        val merged: MutableList<Incident> = Lists.newArrayListWithExpectedSize(2 * maxCount)

        // Map from issue to message to line number to column number to
        // file name to canonical warning
        val map: MutableMap<Issue, MutableMap<String, MutableMap<Int, MutableMap<Int, MutableMap<String, Incident>>>>> =
            Maps.newHashMapWithExpectedSize(2 * maxCount)
        for ((variantName, warnings) in warningMap) {
            for (incident in warnings) {
                val messageMap = map[incident.issue] ?: run {
                    val new: MutableMap<String, MutableMap<Int, MutableMap<Int, MutableMap<String, Incident>>>> =
                        mutableMapOf()
                    map[incident.issue] = new
                    new
                }
                val lineMap = messageMap[incident.message] ?: run {
                    val new: MutableMap<Int, MutableMap<Int, MutableMap<String, Incident>>> =
                        mutableMapOf()
                    messageMap[incident.message] = new
                    new
                }
                val columnMap = lineMap[incident.line] ?: run {
                    val new: MutableMap<Int, MutableMap<String, Incident>> = mutableMapOf()
                    lineMap[incident.line] = new
                    new
                }
                val fileMap = columnMap[incident.startOffset] ?: run {
                    val new: MutableMap<String, Incident> = mutableMapOf()
                    columnMap[incident.startOffset] = new
                    new
                }
                val file = incident.file
                val fileName = run {
                    val parent = file.parentFile
                    if (parent != null) {
                        parent.name + "/" + file.name
                    } else {
                        file.name
                    }
                }
                val canonical = fileMap[fileName] ?: run {
                    fileMap[fileName] = incident
                    incident.applicableVariants = ApplicableVariants(allVariants)
                    merged.add(incident)
                    incident
                }
                canonical.applicableVariants!!.addVariant(variantName)
            }
        }

        // Clear out variants on any nodes that define all
        for (incident in merged) {
            val applicableVariants = incident.applicableVariants ?: continue
            // If this error is present in all variants, just clear it out
            if (!applicableVariants.variantSpecific) {
                incident.applicableVariants = null
            }
        }

        merged.sort()
        return merged
    }

    private fun syncOptions(
        options: LintModelLintOptions?,
        client: IncrementLintClient,
        flags: LintCliFlags,
        variantName: String?,
        project: Project,
        reportsDir: File?,
        report: Boolean,
        fatalOnly: Boolean,
        allowAutoFix: Boolean
    ) {
        if (options != null) {
            syncTo(
                options,
                client,
                flags,
                variantName,
                project,
                reportsDir,
                report
            )
        }
        client.syncConfigOptions()
        if (!allowAutoFix && flags.isAutoFix) {
            flags.isAutoFix = false
        }
        val displayEmpty = !(fatalOnly || flags.isQuiet)
        for (reporter in flags.reporters) {
            reporter.isDisplayEmpty = displayEmpty
        }
    }

    private fun createIssueRegistry(isAndroid: Boolean): BuiltinIssueRegistry {
        return if (isAndroid) {
            BuiltinIssueRegistry()
        } else {
            NonAndroidIssueRegistry()
        }
    }
}
