package com.jianghongkui.lint

import com.android.build.gradle.internal.api.BaseVariantImpl
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author hongkui.jiang* @Date 2019/3/12
 */
class LintPlugin implements Plugin<Project> {

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        MLogger.setOutFile(new File(project.buildDir, "intermediates/LintRun.log").getAbsolutePath())
        if (project.plugins.hasPlugin("com.android.application")) {
            project.android.applicationVariants.all { variant ->
                maybeLint(variant, "App");
            };
        } else if (project.plugins.hasPlugin("com.android.library")) {
            project.android.libraryVariants.all { variant ->
                maybeLint(variant, "Lib");
            };
        } else {
            return
        }

        MLogger.flush();
    }

    private void maybeLint(BaseVariantImpl variant, String type) {
        String suffix = variant.name.capitalize()
        if (!suffix.endsWith("Debug")) {
            return;
        }
        addDebugLintTask(suffix, variant, type);
    }

    private void addDebugLintTask(String suffix, BaseVariantImpl variant, String type) {
        MLogger.addLog("addDebugLintTask-" + suffix + "" + variant + " " + type)
        if (type.equals("App")) {
            setAppLintTask(suffix, variant);
        } else {
            setLibLintTask(suffix, variant);
        }
    }

    private void setAppLintTask(String suffix, BaseVariantImpl variant) {
        //创建FnLintTask 任务
        IncrementLintTask lintTask = project.tasks.replace("AppIncrementLint${suffix}Task", IncrementLintTask)
        lintTask.setApplicationVariant(variant)
        def compileSources = project.tasks["compile${suffix}Sources"]
        lintTask.dependsOn(compileSources)
        def assembleTask = project.tasks["assemble${suffix}"]
        assembleTask.dependsOn(lintTask)
    }

    private void setLibLintTask(String suffix, BaseVariantImpl variant) {
        //创建FnLintTask 任务
        IncrementLintTask lintTask = project.tasks.replace("LibIncrementLint${suffix}Task", IncrementLintTask)
        lintTask.setApplicationVariant(variant)
        def prebuild = project.tasks["pre${suffix}Build"]
        prebuild.dependsOn(lintTask)
    }
}