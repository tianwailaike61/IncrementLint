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

package com.twlk.lib_lint_client_v4_2;

import com.android.repository.Revision;
import com.android.tools.lint.Incident;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintBaseline;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.tools.lint.gradle.KotlinSourceFoldersResolver;
import com.android.tools.lint.gradle.MLintGradleClient;
import com.android.tools.lint.gradle.api.VariantInputs;
import com.twlk.lib_lint_base.LintResultCollector;

import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import kotlin.Pair;

/**
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
public class IncrementLintClient extends MLintGradleClient {

    private final IssueRegistry registry;
    private LintResultCollector collector;

    public IncrementLintClient(String version, IssueRegistry registry, LintCliFlags flags, Project gradleProject,
                               File sdkHome, String variantName, VariantInputs variantInputs, Revision buildToolInfoRevision,
                               KotlinSourceFoldersResolver resolver, boolean isAndroid, String baselineVariantName) {
        super(version, registry, flags, gradleProject, sdkHome, variantName, variantInputs, buildToolInfoRevision, resolver,
                isAndroid, baselineVariantName);
        this.registry = registry;
    }

    @NotNull
    @Override
    protected LintRequest createLintRequest(@NotNull List<? extends File> files) {
        LintRequest request = super.createLintRequest(files);
        for (com.android.tools.lint.detector.api.Project project : request.getProjects()) {
            for (File f : collector.getFiles()) {
                project.addFile(f);
            }
        }
        return request;
    }

    public Pair<List<Incident>, LintBaseline> run(LintResultCollector collector) {
        this.collector = collector;
        return super.run(registry);
    }

    @Override
    public void report(Context context, Issue issue, Severity severity, Location
            location, String message, TextFormat format, LintFix fix) {
        if (collector.addReport(issue.getId(), severity.isError(), context.file)) {
            super.report(context, issue, severity, location, message, format, fix);
        }
    }
}
