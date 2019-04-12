package com.jianghongkui.lint;

import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.tasks.LintBaseTask;
import com.android.builder.model.Variant;
import com.android.sdklib.BuildToolInfo;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.tools.lint.gradle.LintGradleClient;
import com.android.tools.lint.gradle.SyncOptions;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lint增量执行入口
 *
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
class IncrementLintClient extends LintGradleClient {
    private Variant variant;
    private GlobalScope globalScope;
    private Project gradleProject;

    private List<File> files;
    private int errorCount = 0;

    public IncrementLintClient(Project gradleProject, GlobalScope globalScope, VariantScope scope,
                               BuildToolInfo buildToolInfo, Variant variant) {
        super(gradleProject.getGradle().getGradleVersion(), new BuiltinIssueRegistry(), new LintCliFlags(),
                gradleProject, globalScope.getSdkHandler().getSdkFolder(),
                variant, new LintBaseTask.VariantInputs(scope), buildToolInfo, true,
                variant == null ? null : variant.getName());
        this.variant = variant;
        this.globalScope = globalScope;
        this.gradleProject = gradleProject;
    }

    @Override
    public void syncConfigOptions() {
        LintOptions options = globalScope.getExtension().getLintOptions();

        if (options != null) {
            // lint检查报告只需要Html格式
            options.setTextReport(false);
            options.setXmlReport(false);
            options.setAbortOnError(true);
            SyncOptions.syncTo(options, this, getFlags(), variant != null ? variant.getName() : null,
                    gradleProject, globalScope.getReportsDir(), true);
        }
        super.syncConfigOptions();
    }

    @Override
    protected LintRequest createLintRequest(List<File> files) {
        LintRequest request = super.createLintRequest(files);
        if (this.files != null && this.files.size() > 0) {
            for (File file : this.files) { // 将git或svn查询到的修改（或添加）文件加入
                for (com.android.tools.lint.detector.api.Project project : request.getProjects()) {
                    project.addFile(file);
                }
            }
        }
        return request;
    }

    public void run(List<File> files) {
        try {
            this.files = new ArrayList<>(files.size());
            for (File file : files) {
                addFile(file);
            }
            super.run(this.registry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] temp = file.listFiles();
            if (temp != null && temp.length > 0) {
                for (File f : temp) {
                    addFile(f);
                }
            }
        } else {
            this.files.add(file);
        }
    }

    @Override
    public void report(Context context, Issue issue, Severity severity, Location location, String message, TextFormat format, LintFix fix) {
        if ("ObsoleteLintCustomCheck".equals(issue.getId())) return; //过滤自定义lint规则导致的报错
        super.report(context, issue, severity, location, message, format, fix);
        if (issue.getDefaultSeverity() == Severity.ERROR) {
            errorCount++;
        }
    }

    public int getResult() {
        return errorCount;
    }
}
