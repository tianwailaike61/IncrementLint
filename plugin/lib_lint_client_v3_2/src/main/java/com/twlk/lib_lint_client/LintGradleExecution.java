/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
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

package com.twlk.lib_lint_client;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.LintOptions;
import com.android.builder.model.Variant;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.LintFixPerformer;
import com.android.tools.lint.LintStats;
import com.android.tools.lint.Reporter;
import com.android.tools.lint.TextReporter;
import com.android.tools.lint.Warning;
import com.android.tools.lint.XmlReporter;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.checks.UnusedResourceDetector;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintBaseline;
import com.android.tools.lint.gradle.LintGradleClient;
import com.android.tools.lint.gradle.NonAndroidIssueRegistry;
import com.android.tools.lint.gradle.SyncOptions;
import com.android.tools.lint.gradle.api.LintExecutionRequest;
import com.android.tools.lint.gradle.api.VariantInputs;
import com.android.utils.Pair;
import com.twlk.lib_lint_base.LintResultCollector;
import com.twlk.lib_lint_client_v3_2.IncrementLintClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.android.SdkConstants.DOT_XML;
import static com.android.tools.lint.client.api.LintBaseline.VARIANT_ALL;
import static com.android.tools.lint.client.api.LintBaseline.VARIANT_FATAL;
import static com.android.tools.lint.gradle.SyncOptions.createOutputPath;
import static com.android.tools.lint.gradle.SyncOptions.validateOutputFile;

/**
 * Class responsible for driving lint from within Gradle. The purpose of this class is to isolate
 * all lint API access to this single class, such that Gradle can load this driver in its own class
 * loader and thereby have lint itself run in its own class loader, such that classes in the Gradle
 * plugins (such as the Kotlin compiler) does not interfere with classes used by lint (such as a
 * different bundled version of the Kotlin compiler.)
 */
@SuppressWarnings("unused") // Used vi reflection from LintExecutionRequest
public class LintGradleExecution {
    private final LintExecutionRequest descriptor;
    private final LintResultCollector collector;

    public LintGradleExecution(LintExecutionRequest descriptor, LintResultCollector collector) {
        this.descriptor = descriptor;
        this.collector = collector;
    }

    // Along with the constructor, the only public access into this class,
    // intended to be used via reflection. Everything else should be private:
    @SuppressWarnings("unused") // Used via reflection from ReflectiveLintRunner
    public void analyze() throws IOException {
        ToolingModelBuilderRegistry toolingRegistry = descriptor.getToolingRegistry();
        if (toolingRegistry != null) {
            AndroidProject modelProject =
                    createAndroidProject(descriptor.getProject(), toolingRegistry);
            String variantName = descriptor.getVariantName();

            if (variantName != null) {
                for (Variant variant : modelProject.getVariants()) {
                    if (variant.getName().equals(variantName)) {
                        lintSingleVariant(variant);
                        return;
                    }
                }
            } else { // All variants
                lintAllVariants(modelProject);
            }
        } else {
            // Not applying the Android Gradle plugin
            lintNonAndroid();
        }
    }

    @Nullable
    private LintOptions getLintOptions() {
        return descriptor.getLintOptions();
    }

    @Nullable
    private File getSdkHome() {
        return descriptor.getSdkHome();
    }

    private boolean isFatalOnly() {
        return descriptor.isFatalOnly();
    }

    @Nullable
    private File getReportsDir() {
        return descriptor.getReportsDir();
    }

    private void abort(
            @Nullable LintGradleClient client,
            @Nullable List<Warning> warnings,
            boolean isAndroid) {
        String message;
        if (isAndroid) {
            if (isFatalOnly()) {
                message =
                        ""
                                + "Lint found fatal errors while assembling a release target.\n"
                                + "\n"
                                + "To proceed, either fix the issues identified by lint, or modify your build script as follows:\n"
                                + "...\n"
                                + "android {\n"
                                + "    lintOptions {\n"
                                + "        checkReleaseBuilds false\n"
                                + "        // Or, if you prefer, you can continue to check for errors in release builds,\n"
                                + "        // but continue the build even when errors are found:\n"
                                + "        abortOnError false\n"
                                + "    }\n"
                                + "}\n"
                                + "...";
            } else {
                message =
                        ""
                                + "Lint found errors in the project; aborting build.\n"
                                + "\n"
                                + "Fix the issues identified by lint, or add the following to your build script to proceed with errors:\n"
                                + "...\n"
                                + "android {\n"
                                + "    lintOptions {\n"
                                + "        abortOnError false\n"
                                + "    }\n"
                                + "}\n"
                                + "...";
            }
        } else {
            message =
                    ""
                            + "Lint found errors in the project; aborting build.\n"
                            + "\n"
                            + "Fix the issues identified by lint, or add the following to your build script to proceed with errors:\n"
                            + "...\n"
                            + "lintOptions {\n"
                            + "    abortOnError false\n"
                            + "}\n"
                            + "...";
        }

        if (warnings != null
                && client != null
                &&
                // See if there's at least one text reporter
                client.getFlags()
                        .getReporters()
                        .stream()
                        .noneMatch(reporter -> reporter instanceof TextReporter)) {
            List<Warning> errors =
                    warnings.stream()
                            .filter(warning -> warning.severity.isError())
                            .collect(Collectors.toList());
            if (!errors.isEmpty()) {
                String prefix = "Errors found:\n\n";
                if (errors.size() > 3) {
                    // Truncate
                    prefix = "The first 3 errors (out of " + errors.size() + ") were:\n";
                    errors = Arrays.asList(errors.get(0), errors.get(1), errors.get(2));
                }
                StringWriter writer = new StringWriter();
                LintCliFlags flags = client.getFlags();
                flags.setExplainIssues(false);
                TextReporter reporter =
                        Reporter.createTextReporter(client, flags, null, writer, false);
                try {
                    LintStats stats = LintStats.Companion.create(errors.size(), 0);
                    reporter.setWriteStats(false);
                    reporter.write(stats, errors);
                    message += "\n\n" + prefix + writer.toString();
                } catch (IOException ignore) {
                }
            }
        }

        throw new GradleException(message);
    }

    /** Runs lint on the given variant and returns the set of warnings */
    private Pair<List<Warning>, LintBaseline> runLint(
            @Nullable Variant variant,
            @NonNull VariantInputs variantInputs,
            boolean report,
            boolean isAndroid,
            boolean allowFix) {
        IssueRegistry registry = createIssueRegistry(isAndroid);
        LintCliFlags flags = new LintCliFlags();
        IncrementLintClient client =
                new IncrementLintClient(
                        descriptor.getGradlePluginVersion(),
                        registry,
                        flags,
                        descriptor.getProject(),
                        descriptor.getSdkHome(),
                        variant,
                        variantInputs,
                        descriptor.getBuildTools(),
                        isAndroid,
                        variant != null ? variant.getName() : null);
        boolean fatalOnly = descriptor.isFatalOnly();
        if (fatalOnly) {
            flags.setFatalOnly(true);
        }
        LintOptions lintOptions = descriptor.getLintOptions();
        boolean fix = false;
        if (lintOptions != null) {
            // IDEA: Find out if we're on a CI server (how? $DISPLAY available etc?)
            // and if so turn off auto-suggest. Other clues include setting
            // temp dir etc.
            syncOptions(
                    lintOptions,
                    client,
                    flags,
                    variant,
                    descriptor.getProject(),
                    descriptor.getReportsDir(),
                    report,
                    fatalOnly,
                    allowFix);
        } else {
            // Set up some default reporters
            flags.getReporters()
                    .add(
                            Reporter.createTextReporter(
                                    client, flags, null, new PrintWriter(System.out, true), false));
            File html =
                    validateOutputFile(
                            createOutputPath(
                                    descriptor.getProject(),
                                    null,
                                    ".html",
                                    null,
                                    flags.isFatalOnly()));
            File xml =
                    validateOutputFile(
                            createOutputPath(
                                    descriptor.getProject(),
                                    null,
                                    DOT_XML,
                                    null,
                                    flags.isFatalOnly()));
            try {
                flags.getReporters().add(Reporter.createHtmlReporter(client, html, flags));
                flags.getReporters().add(Reporter.createXmlReporter(client, xml, false));
            } catch (IOException e) {
                throw new GradleException(e.getMessage(), e);
            }
        }
        if (!report || fatalOnly) {
            flags.setQuiet(true);
        }
        flags.setWriteBaselineIfMissing(report && !fatalOnly);

        Pair<List<Warning>, LintBaseline> warnings;

        if (allowFix & descriptor.getAutoFix()) { // Explicit fix Gradle target
            flags.setAutoFix(true);
        }

        try {
            warnings = client.run(collector);
        } catch (IOException e) {
            throw new GradleException("Invalid arguments.", e);
        }

//        if (report && client.haveErrors() && flags.isSetExitCode()) {
//            abort(client, warnings.getFirst(), isAndroid);
//        }

        return warnings;
    }

    private static void syncOptions(
            @Nullable LintOptions options,
            @NonNull LintGradleClient client,
            @NonNull LintCliFlags flags,
            @Nullable Variant variant,
            @NonNull Project project,
            @Nullable File reportsDir,
            boolean report,
            boolean fatalOnly,
            boolean allowAutoFix) {
        if (options != null) {
            SyncOptions.syncTo(
                    options,
                    client,
                    flags,
                    variant != null ? variant.getName() : null,
                    project,
                    reportsDir,
                    report);
        }

        client.syncConfigOptions();

        if (!allowAutoFix && flags.isAutoFix()) {
            flags.setAutoFix(false);
        }

        boolean displayEmpty = !(fatalOnly || flags.isQuiet());
        for (Reporter reporter : flags.getReporters()) {
            reporter.setDisplayEmpty(displayEmpty);
        }
    }

    protected static AndroidProject createAndroidProject(
            @NonNull Project gradleProject, @NonNull ToolingModelBuilderRegistry toolingRegistry) {
        String modelName = AndroidProject.class.getName();
        ToolingModelBuilder modelBuilder = toolingRegistry.getBuilder(modelName);

        // setup the level 3 sync.
        final ExtraPropertiesExtension ext = gradleProject.getExtensions().getExtraProperties();
        ext.set(
                AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED,
                Integer.toString(AndroidProject.MODEL_LEVEL_3_VARIANT_OUTPUT_POST_BUILD));
        ext.set(AndroidProject.PROPERTY_BUILD_MODEL_DISABLE_SRC_DOWNLOAD, true);

        try {
            return (AndroidProject) modelBuilder.buildAll(modelName, gradleProject);
        } finally {
            ext.set(AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED, null);
            ext.set(AndroidProject.PROPERTY_BUILD_MODEL_DISABLE_SRC_DOWNLOAD, null);
        }
    }

    private static BuiltinIssueRegistry createIssueRegistry(boolean isAndroid) {
        if (isAndroid) {
            return new BuiltinIssueRegistry();
        } else {
            return new NonAndroidIssueRegistry();
        }
    }

    /** Runs lint on a single specified variant */
    public void lintSingleVariant(@NonNull Variant variant) {
        VariantInputs variantInputs = descriptor.getVariantInputs(variant.getName());
        if (variantInputs != null) {
            runLint(variant, variantInputs, true, true, true);
        }
    }

    /**
     * Runs lint for a non-Android project (such as a project that only applies the Kotlin Gradle
     * plugin, not the Android Gradle plugin
     */
    public void lintNonAndroid() {
        VariantInputs variantInputs = descriptor.getVariantInputs("");
        if (variantInputs != null) {
            runLint(null, variantInputs, true, false, true);
        }
    }

    /**
     * Runs lint individually on all the variants, and then compares the results across variants and
     * reports these
     */
    public void lintAllVariants(@NonNull AndroidProject modelProject) throws IOException {
        // In the Gradle integration we iterate over each variant, and
        // attribute unused resources to each variant, so don't make
        // each variant run go and inspect the inactive variant sources
        UnusedResourceDetector.sIncludeInactiveReferences = false;

        Map<Variant, List<Warning>> warningMap = Maps.newHashMap();
        List<LintBaseline> baselines = Lists.newArrayList();
        boolean first = true;
        for (Variant variant : modelProject.getVariants()) {
            // we are not running lint on all the variants, so skip the ones where we don't have
            // a variant inputs (see TaskManager::isLintVariant)
            final VariantInputs variantInputs = descriptor.getVariantInputs(variant.getName());
            if (variantInputs != null) {
                Pair<List<Warning>, LintBaseline> pair =
                        runLint(variant, variantInputs, false, true, first);
                first = false;
                List<Warning> warnings = pair.getFirst();
                warningMap.put(variant, warnings);
                LintBaseline baseline = pair.getSecond();
                if (baseline != null) {
                    baselines.add(baseline);
                }
            }
        }

        final LintOptions lintOptions = getLintOptions();

        // Compute error matrix
        boolean quiet = false;
        if (lintOptions != null) {
            quiet = lintOptions.isQuiet();
        }

        for (Map.Entry<Variant, List<Warning>> entry : warningMap.entrySet()) {
            Variant variant = entry.getKey();
            List<Warning> warnings = entry.getValue();
            if (!isFatalOnly() && !quiet) {
                descriptor.warn(
                        "Ran lint on variant {}: {} issues found",
                        variant.getName(),
                        warnings.size());
            }
        }

        List<Warning> mergedWarnings = LintGradleClient.merge(warningMap, modelProject);
        LintStats stats = LintStats.Companion.create(mergedWarnings, baselines);
        int errorCount = stats.getErrorCount();

        // We pick the first variant to generate the full report and don't generate if we don't
        // have any variants.
        if (!modelProject.getVariants().isEmpty()) {
            Set<Variant> allVariants = Sets.newTreeSet(Comparator.comparing(Variant::getName));

            allVariants.addAll(modelProject.getVariants());
            Variant variant = allVariants.iterator().next();

            IssueRegistry registry = new BuiltinIssueRegistry();
            LintCliFlags flags = new LintCliFlags();
            VariantInputs variantInputs = descriptor.getVariantInputs(variant.getName());
            assert variantInputs != null : variant.getName();
            LintGradleClient client =
                    new LintGradleClient(
                            descriptor.getGradlePluginVersion(),
                            registry,
                            flags,
                            descriptor.getProject(),
                            getSdkHome(),
                            variant,
                            variantInputs,
                            descriptor.getBuildTools(),
                            true,
                            isFatalOnly() ? VARIANT_FATAL : VARIANT_ALL);
            syncOptions(
                    lintOptions,
                    client,
                    flags,
                    null,
                    descriptor.getProject(),
                    getReportsDir(),
                    true,
                    isFatalOnly(),
                    true);

            // When running the individual variant scans we turn off auto fixing
            // so perform it manually here when we have the merged results
            if (flags.isAutoFix()) {
                new LintFixPerformer(client, !flags.isQuiet()).fix(mergedWarnings);
            }

            for (Reporter reporter : flags.getReporters()) {
                reporter.write(stats, mergedWarnings);
            }

            File baselineFile = flags.getBaselineFile();
            if (baselineFile != null && !baselineFile.exists()) {
                File dir = baselineFile.getParentFile();
                boolean ok = true;
                if (!dir.isDirectory()) {
                    ok = dir.mkdirs();
                }
                if (!ok) {
                    System.err.println("Couldn't create baseline folder " + dir);
                } else {
                    XmlReporter reporter = Reporter.createXmlReporter(client, baselineFile, true);
                    reporter.setBaselineAttributes(
                            client, flags.isFatalOnly() ? VARIANT_FATAL : VARIANT_ALL);
                    reporter.write(stats, mergedWarnings);
                    System.err.println("Created baseline file " + baselineFile);
                    if (LintGradleClient.continueAfterBaseLineCreated()) {
                        return;
                    }
                    System.err.println("(Also breaking build in case this was not intentional.)");
                    String message = client.getBaselineCreationMessage(baselineFile);
                    throw new GradleException(message);
                }
            }

            LintBaseline firstBaseline = baselines.isEmpty() ? null : baselines.get(0);
            if (baselineFile != null && firstBaseline != null) {
                client.emitBaselineDiagnostics(firstBaseline, baselineFile, stats);
            }

            if (flags.isSetExitCode() && errorCount > 0) {
                abort(client, mergedWarnings, true);
            }
        }
    }
}
