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

package com.twlk.lib_lint_client;

import com.android.builder.model.AndroidProject;
import com.android.builder.model.LintOptions;
import com.android.builder.model.ProjectSyncIssues;
import com.android.builder.model.Variant;
import com.android.ide.common.gradle.model.IdeAndroidProject;
import com.android.ide.common.gradle.model.IdeAndroidProjectImpl;
import com.android.ide.common.gradle.model.level2.IdeDependenciesFactory;
import com.android.repository.Revision;
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
import com.android.tools.lint.gradle.KotlinSourceFoldersResolver;
import com.android.tools.lint.gradle.LintGradleClient;
import com.android.tools.lint.gradle.NonAndroidIssueRegistry;
import com.android.tools.lint.gradle.SyncOptions;
import com.android.tools.lint.gradle.api.LintExecutionRequest;
import com.android.tools.lint.gradle.api.VariantInputs;
import com.android.utils.Pair;
import com.twlk.lib_lint_base.LintResultCollector;
import com.twlk.lib_lint_client_v3_6.IncrementLintClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.twlk.lib_lint_client_v3_6.IncrementLintClient;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

public class LintGradleExecution {
    private final LintExecutionRequest descriptor;
    private final LintResultCollector collector;

    public LintGradleExecution(LintExecutionRequest descriptor, LintResultCollector collector) {
        this.descriptor = descriptor;
        this.collector = collector;
    }

    public void analyze() throws IOException {
        ToolingModelBuilderRegistry toolingRegistry = this.descriptor.getToolingRegistry();
        if (toolingRegistry != null) {
            IdeAndroidProject modelProject = createAndroidProject(this.descriptor.getProject(), toolingRegistry);
            String variantName = this.descriptor.getVariantName();
            if (variantName != null) {
                Iterator var4 = modelProject.getVariants().iterator();

                while (var4.hasNext()) {
                    Variant variant = (Variant) var4.next();
                    if (variant.getName().equals(variantName)) {
                        this.lintSingleVariant(variant);
                        return;
                    }
                }
            } else {
                this.lintAllVariants(modelProject);
            }
        } else {
            this.lintNonAndroid();
        }

    }

    private LintOptions getLintOptions() {
        return this.descriptor.getLintOptions();
    }

    private File getSdkHome() {
        return this.descriptor.getSdkHome();
    }

    private boolean isFatalOnly() {
        return this.descriptor.isFatalOnly();
    }

    private File getReportsDir() {
        return this.descriptor.getReportsDir();
    }

    private void abort(LintGradleClient client, List<Warning> warnings, boolean isAndroid) {
        String message;
        if (isAndroid) {
            if (this.isFatalOnly()) {
                message = "Lint found fatal errors while assembling a release target.\n\nTo proceed, either fix the issues identified by lint, or modify your build script as follows:\n...\nandroid {\n    lintOptions {\n        checkReleaseBuilds false\n        // Or, if you prefer, you can continue to check for errors in release builds,\n        // but continue the build even when errors are found:\n        abortOnError false\n    }\n}\n...";
            } else {
                message = "Lint found errors in the project; aborting build.\n\nFix the issues identified by lint, or add the following to your build script to proceed with errors:\n...\nandroid {\n    lintOptions {\n        abortOnError false\n    }\n}\n...";
            }
        } else {
            message = "Lint found errors in the project; aborting build.\n\nFix the issues identified by lint, or add the following to your build script to proceed with errors:\n...\nlintOptions {\n    abortOnError false\n}\n...";
        }

        if (warnings != null && client != null && client.getFlags().getReporters().stream().noneMatch((reporterx) -> {
            return reporterx instanceof TextReporter;
        })) {
            List<Warning> errors = (List) warnings.stream().filter((warning) -> {
                return warning.severity.isError();
            }).collect(Collectors.toList());
            if (!errors.isEmpty()) {
                String prefix = "Errors found:\n\n";
                if (errors.size() > 3) {
                    prefix = "The first 3 errors (out of " + errors.size() + ") were:\n";
                    errors = Arrays.asList((Warning) errors.get(0), (Warning) errors.get(1), (Warning) errors.get(2));
                }

                StringWriter writer = new StringWriter();
                LintCliFlags flags = client.getFlags();
                flags.setExplainIssues(false);
                TextReporter reporter = Reporter.createTextReporter(client, flags, (File) null, writer, false);

                try {
                    LintStats stats = LintStats.Companion.create(errors.size(), 0);
                    reporter.setWriteStats(false);
                    reporter.write(stats, errors);
                    message = message + "\n\n" + prefix + writer.toString();
                } catch (IOException var11) {
                }
            }
        }

        throw new GradleException(message);
    }

    private Pair<List<Warning>, LintBaseline> runLint(Variant variant, VariantInputs variantInputs, boolean report, boolean isAndroid, boolean allowFix) {
        IssueRegistry registry = createIssueRegistry(isAndroid);
        LintCliFlags flags = new LintCliFlags();
        String var10002 = this.descriptor.getGradlePluginVersion();
        Project var10005 = this.descriptor.getProject();
        File var10006 = this.descriptor.getSdkHome();
        Revision var10009 = this.descriptor.getBuildToolsRevision();
        LintExecutionRequest var10012 = this.descriptor;
        var10012.getClass();
        IncrementLintClient client = new IncrementLintClient(var10002, registry, flags, var10005, var10006, variant, variantInputs, var10009, new KotlinSourceFoldersResolver(var10012::getKotlinSourceFolders), isAndroid, variant != null ? variant.getName() : null);
        boolean fatalOnly = this.descriptor.isFatalOnly();
        if (fatalOnly) {
            flags.setFatalOnly(true);
        }

        boolean autoFixing = allowFix & this.descriptor.getAutoFix();
        LintOptions lintOptions = this.descriptor.getLintOptions();
        boolean fix = false;
        if (lintOptions != null) {
            syncOptions(lintOptions, client, flags, variant, this.descriptor.getProject(), this.descriptor.getReportsDir(), report, fatalOnly, allowFix);
        } else {
            flags.getReporters().add(Reporter.createTextReporter(client, flags, (File) null, new PrintWriter(System.out, true), false));
            if (!autoFixing) {
                File html = SyncOptions.validateOutputFile(SyncOptions.createOutputPath(this.descriptor.getProject(), (String) null, ".html", (File) null, flags.isFatalOnly()));
                File xml = SyncOptions.validateOutputFile(SyncOptions.createOutputPath(this.descriptor.getProject(), (String) null, ".xml", (File) null, flags.isFatalOnly()));

                try {
                    flags.getReporters().add(Reporter.createHtmlReporter(client, html, flags));
                    flags.getReporters().add(Reporter.createXmlReporter(client, xml, false, flags.isIncludeXmlFixes()));
                } catch (IOException var17) {
                    throw new GradleException(var17.getMessage(), var17);
                }
            }
        }

        if (!report || fatalOnly) {
            flags.setQuiet(true);
        }

        flags.setWriteBaselineIfMissing(report && !fatalOnly && !autoFixing);
        if (autoFixing) {
            flags.setAutoFix(true);
            flags.setSetExitCode(false);
        }

        Pair warnings;
        try {
            warnings = client.run(collector);
        } catch (IOException var16) {
            throw new GradleException("Invalid arguments.", var16);
        }

//        if (report && client.haveErrors() && flags.isSetExitCode()) {
//            this.abort(client, (List) warnings.getFirst(), isAndroid);
//        }

        return warnings;
    }

    private static void syncOptions(LintOptions options, LintGradleClient client, LintCliFlags flags, Variant variant, Project project, File reportsDir, boolean report, boolean fatalOnly, boolean allowAutoFix) {
        if (options != null) {
            SyncOptions.syncTo(options, client, flags, variant != null ? variant.getName() : null, project, reportsDir, report);
        }

        client.syncConfigOptions();
        if (!allowAutoFix && flags.isAutoFix()) {
            flags.setAutoFix(false);
        }

        boolean displayEmpty = !fatalOnly && !flags.isQuiet();
        Iterator var10 = flags.getReporters().iterator();

        while (var10.hasNext()) {
            Reporter reporter = (Reporter) var10.next();
            reporter.setDisplayEmpty(displayEmpty);
        }

    }

    protected static IdeAndroidProject createAndroidProject(Project gradleProject, ToolingModelBuilderRegistry toolingRegistry) {
        String modelName = AndroidProject.class.getName();
        ToolingModelBuilder modelBuilder = toolingRegistry.getBuilder(modelName);

        assert modelBuilder.canBuild(modelName) : modelName;

        ExtraPropertiesExtension ext = gradleProject.getExtensions().getExtraProperties();
        synchronized (ext) {
            ext.set("android.injected.build.model.only.versioned", Integer.toString(3));

            IdeAndroidProjectImpl var7;
            try {
                AndroidProject project = (AndroidProject) modelBuilder.buildAll(modelName, gradleProject);
                var7 = IdeAndroidProjectImpl.create(project, new IdeDependenciesFactory(), project.getVariants(), (ProjectSyncIssues) null);
            } finally {
                ext.set("android.injected.build.model.only.versioned", (Object) null);
            }

            return var7;
        }
    }

    private static BuiltinIssueRegistry createIssueRegistry(boolean isAndroid) {
        return (BuiltinIssueRegistry) (isAndroid ? new BuiltinIssueRegistry() : new NonAndroidIssueRegistry());
    }

    public void lintSingleVariant(Variant variant) {
        VariantInputs variantInputs = this.descriptor.getVariantInputs(variant.getName());
        if (variantInputs != null) {
            this.runLint(variant, variantInputs, true, true, true);
        }

    }

    public void lintNonAndroid() {
        VariantInputs variantInputs = this.descriptor.getVariantInputs("");
        if (variantInputs != null) {
            this.runLint((Variant) null, variantInputs, true, false, true);
        }

    }

    public void lintAllVariants(IdeAndroidProject modelProject) throws IOException {
        UnusedResourceDetector.sIncludeInactiveReferences = false;
        Map<Variant, List<Warning>> warningMap = Maps.newHashMap();
        List<LintBaseline> baselines = Lists.newArrayList();
        boolean first = true;
        Iterator var5 = modelProject.getVariants().iterator();

        while (var5.hasNext()) {
            Variant variant = (Variant) var5.next();
            VariantInputs variantInputs = this.descriptor.getVariantInputs(variant.getName());
            if (variantInputs != null) {
                Pair<List<Warning>, LintBaseline> pair = this.runLint(variant, variantInputs, false, true, first);
                first = false;
                List<Warning> warnings = (List) pair.getFirst();
                warningMap.put(variant, warnings);
                LintBaseline baseline = (LintBaseline) pair.getSecond();
                if (baseline != null) {
                    baselines.add(baseline);
                }
            }
        }

        LintOptions lintOptions = this.getLintOptions();
        boolean quiet = false;
        if (lintOptions != null) {
            quiet = lintOptions.isQuiet();
        }

        Iterator var23 = warningMap.entrySet().iterator();

        while (var23.hasNext()) {
            Entry<Variant, List<Warning>> entry = (Entry) var23.next();
            Variant variant = (Variant) entry.getKey();
            List<Warning> warnings = (List) entry.getValue();
            if (!this.isFatalOnly() && !quiet) {
                this.descriptor.warn("Ran lint on variant {}: {} issues found", new Object[]{variant.getName(), warnings.size()});
            }
        }

        List<Warning> mergedWarnings = LintGradleClient.merge(warningMap, modelProject);
        LintStats stats = LintStats.Companion.create(mergedWarnings, baselines);
        int errorCount = stats.getErrorCount();
        if (!modelProject.getVariants().isEmpty()) {
            Set<Variant> allVariants = Sets.newTreeSet(Comparator.comparing(Variant::getName));
            allVariants.addAll(modelProject.getVariants());
            Variant variant = (Variant) allVariants.iterator().next();
            IssueRegistry registry = new BuiltinIssueRegistry();
            LintCliFlags flags = new LintCliFlags();
            VariantInputs variantInputs = this.descriptor.getVariantInputs(variant.getName());

            assert variantInputs != null : variant.getName();

            String var10002 = this.descriptor.getGradlePluginVersion();
            Project var10005 = this.descriptor.getProject();
            File var10006 = this.getSdkHome();
            Revision var10009 = this.descriptor.getBuildToolsRevision();
            LintExecutionRequest var10012 = this.descriptor;
            var10012.getClass();
            LintGradleClient client = new LintGradleClient(var10002, registry, flags, var10005, var10006, variant, variantInputs, var10009, new KotlinSourceFoldersResolver(var10012::getKotlinSourceFolders), true, this.isFatalOnly() ? "fatal" : "all");
            syncOptions(lintOptions, client, flags, (Variant) null, this.descriptor.getProject(), this.getReportsDir(), true, this.isFatalOnly(), true);
            if (flags.isAutoFix()) {
                flags.setSetExitCode(false);
                (new LintFixPerformer(client, !flags.isQuiet())).fix(mergedWarnings);
            }

            Iterator var16 = flags.getReporters().iterator();

            while (var16.hasNext()) {
                Reporter reporter = (Reporter) var16.next();
                reporter.write(stats, mergedWarnings);
            }

            File baselineFile = flags.getBaselineFile();
            if (baselineFile != null && !baselineFile.exists() && !flags.isAutoFix()) {
                File dir = baselineFile.getParentFile();
                boolean ok = true;
                if (!dir.isDirectory()) {
                    ok = dir.mkdirs();
                }

                if (ok) {
                    XmlReporter reporter = Reporter.createXmlReporter(client, baselineFile, true, false);
                    reporter.setBaselineAttributes(client, flags.isFatalOnly() ? "fatal" : "all");
                    reporter.write(stats, mergedWarnings);
                    System.err.println("Created baseline file " + baselineFile);
                    if (LintGradleClient.continueAfterBaseLineCreated()) {
                        return;
                    }

                    System.err.println("(Also breaking build in case this was not intentional.)");
                    String message = client.getBaselineCreationMessage(baselineFile);
                    throw new GradleException(message);
                }

                System.err.println("Couldn't create baseline folder " + dir);
            }

            LintBaseline firstBaseline = baselines.isEmpty() ? null : (LintBaseline) baselines.get(0);
            if (baselineFile != null && firstBaseline != null) {
                client.emitBaselineDiagnostics(firstBaseline, baselineFile, stats);
            }

            if (flags.isSetExitCode() && errorCount > 0) {
                this.abort(client, mergedWarnings, true);
            }
        }

    }
}

