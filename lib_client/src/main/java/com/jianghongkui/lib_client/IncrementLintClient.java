package com.jianghongkui.lib_client;


import com.android.builder.model.Variant;
import com.android.sdklib.BuildToolInfo;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.tools.lint.gradle.LintGradleClient;
import com.android.tools.lint.gradle.api.VariantInputs;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hongkui.jiang
 * @Date 2019-04-25
 */
public class IncrementLintClient extends LintGradleClient {

    private Set<String> resultFilePaths;

    public IncrementLintClient(String version, IssueRegistry registry, LintCliFlags flags, Project gradleProject, File sdkHome, Variant variant, VariantInputs variantInputs, BuildToolInfo buildToolInfo, boolean isAndroid, String baselineVariantName) {
        super(version, registry, flags, gradleProject, sdkHome, variant, variantInputs, buildToolInfo, isAndroid, baselineVariantName);
        resultFilePaths = new HashSet<>();
    }

    @Override
    protected LintRequest createLintRequest(List<File> files) {
        LintRequest request = super.createLintRequest(Collections.emptyList());
        if (files != null && files.size() > 0) {
            for (File file : files) { // 将git或svn查询到的修改（或添加）文件加入
                for (com.android.tools.lint.detector.api.Project project : request.getProjects()) {
                    project.addFile(file);
                }
            }
        }
        return request;
    }

    public int run(Collection<File> files) {
        if (files == null || files.isEmpty()) {
            return 0;
        }
        try {
            List<File> list = new ArrayList<>(files.size());
            list.addAll(files);
            super.run(this.registry, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errorCount;
    }


    @Override
    public void report(Context context, Issue issue, Severity severity, Location location, String message, TextFormat format, LintFix fix) {
        if ("ObsoleteLintCustomCheck".equals(issue.getId())) return; //过滤自定义lint规则导致的报错
        super.report(context, issue, severity, location, message, format, fix);
        if (issue.getDefaultSeverity() == Severity.ERROR) {
            resultFilePaths.add(context.file.getAbsolutePath());
        }
    }

    public Collection<String> getResult() {
        return resultFilePaths;
    }
}
